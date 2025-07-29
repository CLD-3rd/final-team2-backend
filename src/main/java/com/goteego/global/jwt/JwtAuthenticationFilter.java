package com.goteego.global.jwt;

import com.goteego.global.error.exception.AccessDeniedException;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
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
            "/api/feed/**",         // GET만 허용
            "/ws/**"
    );
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        AntPathMatcher pathMatcher = new AntPathMatcher();

        // ✅ GET 요청일 때만 화이트리스트 매칭
        if (method.equalsIgnoreCase("GET")) {
            for (String pattern : WHITELIST) {
                if (pathMatcher.match(pattern, uri)) {
                    log.debug("✅ JWT 필터 스킵 (화이트리스트): {}", uri);
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

        log.info("🔍 [JwtAuthenticationFilter] 요청 URI: {}", uri);

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // Access Token 유효
                log.info("✅ Access Token 유효: 인증 처리 시작");
                setAuthenticationFromAccessToken(accessToken, request);

            } else if (refreshToken != null) {
                // Access Token 만료 or 없음 → Refresh Token 검사
                log.warn("⚠️ Access Token 만료 또는 없음, Refresh Token으로 인증 시도");

                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                User user = userRepository.findByOauthInfo_OauthEmail(email)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

                // Refresh Token 만료 여부 확인
                if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                    log.warn("❌ Refresh Token 만료됨: {}", email);
                    throw new AccessDeniedException(ErrorCode.EXPIRED_TOKEN);
                }

                // Refresh Token 불일치
                if (!refreshToken.equals(userRepository.findRefreshTokenByUserId(user.getId()))) {
                    log.warn("❌ Refresh Token 불일치: {}", email);
                    throw new AccessDeniedException(ErrorCode.RT_NOT_FOUND);
                }

                // 새로운 Access Token 발급
                String newAccessToken = jwtTokenProvider.createAccessToken(user);

                Cookie newAccessTokenCookie = CookieUtil.createCookieForLocal("accessToken", newAccessToken, jwtTokenProvider.getAccessTokenMaxAgeInSeconds());

                response.addCookie(newAccessTokenCookie);

                setAuthenticationFromAccessToken(newAccessToken, request);
                log.info("🔄 Access Token 재발급 완료 for user: {}", email);
                // 수정한 부분
            } else {
                log.info("🔒 토큰 없음—익명 사용자로 진행");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

        } catch (BusinessException e) {
            log.warn("🚫 [JWT Filter] - {}: {}", e.getErrorCode(), e.getMessage());
            setErrorResponse(response, e.getErrorCode(), request.getRequestURI());
            return; // ❗ 더 이상 필터 체인을 진행하지 않음
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationFromAccessToken(String token, HttpServletRequest request) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByOauthInfo_OauthEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getRole().getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("🔐 사용자 인증 성공: {}", email);
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