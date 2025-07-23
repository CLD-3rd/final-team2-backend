package com.goteego.global.auth;

import com.goteego.global.jwt.JwtTokenProvider;
import com.goteego.global.util.CookieUtil;
import com.goteego.user.domain.CustomOAuth2User;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("[OAuth2SuccessHandler] 소셜 로그인 성공");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        log.info("✅ [OAuth2SuccessHandler] 사용자 인증 성공: {}", user.getOauthInfo().getOauthEmail());

        // ✅ JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // ✅ RefreshToken 저장
        userService.updateRefreshToken(user.getId(), refreshToken);
        log.info("✅ [OAuth2SuccessHandler] RefreshToken 저장 성공");

        // ✅ 쿠키 생성 및 응답에 추가
        Cookie accessTokenCookie = CookieUtil.createCookieForLocal("accessToken", accessToken, jwtTokenProvider.getAccessTokenMaxAgeInSeconds());
        Cookie refreshTokenCookie = CookieUtil.createCookieForLocal("refreshToken", refreshToken, jwtTokenProvider.getRefreshTokenMaxAgeInSeconds());

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("✅ [OAuth2SuccessHandler] Token 쿠키로 전송 완료");

        // ✅ 리디렉션
        response.sendRedirect("/test.html");
    }
}