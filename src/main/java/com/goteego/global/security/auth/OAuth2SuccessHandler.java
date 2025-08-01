package com.goteego.global.security.auth;

import com.goteego.global.security.jwt.JwtTokenProvider;
import com.goteego.global.security.jwt.RefreshTokenService;
import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.CustomOAuth2User;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService; // ✅ 추가

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("✅ [OAuth2Success] 소셜 로그인 성공");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        log.info("✅ [OAuth2Success] 사용자 인증 성공: {}", user.getOauthInfo().getOauthEmail());

        // ✅ JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // ✅ Redis에 RefreshToken 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        log.info("✅ [OAuth2Success] RefreshToken 저장 성공: userId = {}", user.getId());

        // ✅ 쿠키 생성 및 응답에 추가
        response.addCookie(CookieUtil.createCookieForLocal("accessToken", accessToken, jwtTokenProvider.getAccessTokenMaxAgeInSeconds()));
        response.addCookie(CookieUtil.createCookieForLocal("refreshToken", refreshToken, jwtTokenProvider.getRefreshTokenMaxAgeInSeconds()));
        log.info("✅ [OAuth2Success] Token 쿠키로 전송 완료");
        // ✅ 리디렉션
//        response.sendRedirect("/dashboard.html");
//        response.sendRedirect("/post.html");

        // ✅ 프론트엔드로 리다이렉트
        response.sendRedirect(frontendUrl);
    }
}