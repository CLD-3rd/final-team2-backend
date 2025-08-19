package com.goteego.global.security.jwt;

import com.goteego.global.error.exception.AccessDeniedException;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> WHITELIST = List.of(
            "/",
            "/login/**",
            "/index/**",
            "/oauth2/**",
            "/.well-known/**",
            "/api/travel-posts/**", // GET만 허용
            "/api/feeds/**",         // GET만 허용
            "/ws/**",
            "/index.html"
    );
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService; // ✅ 추가

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        AntPathMatcher pathMatcher = new AntPathMatcher();

        // 🚫 actuator는 무조건 스킵
        if (pathMatcher.match("/actuator/**", uri)) {
            log.debug("✅ [JwtFilter] actuator 경로 스킵: {}", uri);
            return true;
        }
        
        // ✅ GET 요청일 때만 화이트리스트 매칭
        if (method.equalsIgnoreCase("GET")) {
            for (String pattern : WHITELIST) {
                if (pathMatcher.match(pattern, uri)) {
                    log.debug("✅ [JwtFilter] JWT 필터 스킵 (화이트리스트): {}", uri);

                    // 화이트리스트 요청이라도 accessToken이 있다면 인증을 설정해야 함
                    String accessToken = CookieUtil.getTokenFromCookie(request, "accessToken");
                    if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                        setAuthenticationFromAccessToken(accessToken, request);
                        return false;  // 화이트리스트여도 인증을 했으므로 필터를 건너뛰지 않음
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String accessToken = CookieUtil.getTokenFromCookie(request, "accessToken");
        String refreshToken = CookieUtil.getTokenFromCookie(request, "refreshToken");

        log.info("🔍 [JwtFilter] 요청 URI: {}", uri);

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // Access Token 유효
                log.info("✅ [JwtFilter] Access Token 유효 → 인증");
                setAuthenticationFromAccessToken(accessToken, request);

            } else if (refreshToken != null) {
                // Access Token 만료 or 없음 → Refresh Token 검사
                log.warn("⚠️ [JwtFilter] Access Token 만료 → Refresh Token 검증");

                Long userId = jwtTokenProvider.getUserId(refreshToken);

                // Refresh Token 만료 여부 확인
                if (jwtTokenProvider.isExpired(refreshToken)) {
                    log.warn("❌ [JwtFilter] Refresh Token 만료: userId={}", userId);
                    throw new AccessDeniedException(ErrorCode.EXPIRED_TOKEN);
                }

                String storedRefreshToken = refreshTokenService.getRefreshToken(userId)
                        .orElseThrow(() -> new AccessDeniedException(ErrorCode.RT_NOT_FOUND));

                // Refresh Token 불일치
                if (!refreshToken.equals(storedRefreshToken)) {
                    log.warn("❌ [JwtFilter] Refresh Token 불일치: userId={}", userId);
                    throw new AccessDeniedException(ErrorCode.RT_NOT_FOUND);
                }

                // 새로운 Access Token 발급
                String newAccessToken = jwtTokenProvider.createAccessTokenFromRefresh(refreshToken);
                Cookie newAccessTokenCookie = CookieUtil.createCookieForLocal(
                        "accessToken", newAccessToken,
                        jwtTokenProvider.getAccessTokenMaxAgeInSeconds());
                response.addCookie(newAccessTokenCookie);

                setAuthenticationFromAccessToken(newAccessToken, request);
                log.info("🔄 [JwtFilter] Access Token 재발급 완료 for userId={}", userId);

            } else {
                log.info("🔒 [JwtFilter] 토큰 없음 → 익명 사용자");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

        } catch (BusinessException e) {
            log.warn("🚫 [JwtFilter] JWT 인증 실패: {}", e.getMessage());
            setErrorResponse(response, e.getErrorCode(), request.getRequestURI());
            return; // ❗ 더 이상 필터 체인을 진행하지 않음
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationFromAccessToken(String token, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserId(token);
        String role = jwtTokenProvider.getRole(token);

        // ✅ DB에서 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        // ✅ Principal로 User 객체를 넣음
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("🔐 [JwtFilter] 인증 성공: userId={}, role={}", userId, role);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, String path) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = String.format("""
                {
                  "status": %d,
                  "error": "%s",
                  "path": "%s"
                }
                """, errorCode.getStatus().value(), errorCode.getMessage(), path);

        response.getWriter().write(body);
    }
}