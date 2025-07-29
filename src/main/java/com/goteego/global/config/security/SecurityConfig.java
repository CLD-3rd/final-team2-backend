package com.goteego.global.config.security;

import com.goteego.global.auth.OAuth2SuccessHandler;
import com.goteego.global.jwt.JwtAuthenticationFilter;
import com.goteego.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/",
            "/favicon.ico",
            "/api/public/**",
            "/api/feed/**",  // 임시 추가
            "/api/users/me",
            "/api/schedule/**",      // 임시 추가
            "/api/recommendation/**", // 임시 추가
            "/error",
            "/index.html",
            "/oauth2/**",
            "/login",
            "/.well-known/**",
            "/static/**",
            "/ws/**"
    };

    private final CustomOAuth2UserService customOAuth2UserService;
    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        log.info("[SecurityConfig] 기본 보안 설정 시작");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ✅ 여행 게시글: GET 요청은 허용, 나머지는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/travel-posts", "/api/travel-posts/**").permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // ✅ 사용자 정보 처리
                        .successHandler(oAuth2SuccessHandler) // ✅ 성공 핸들러 등록
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl)); // * 대신 명시적으로 작성
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}