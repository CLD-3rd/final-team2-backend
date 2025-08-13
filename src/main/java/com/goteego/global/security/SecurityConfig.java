package com.goteego.global.security;

import com.goteego.global.security.auth.OAuth2SuccessHandler;
import com.goteego.global.security.jwt.JwtAuthenticationFilter;
import com.goteego.global.web.FrontendProperties;
import com.goteego.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    // 임시 공개 URL
    private static final String[] PUBLIC_URLS = {
            "/api/schedule/**", "/api/recommendation/**", "/api/schedule/**", "/api/users/**",
            "/api/recommendation/**", "/api/review/**", "/api/user/badge/**", "/api/admin/badge/**",
    };

    private static final String[] PUBLIC_GET_URLS = {
            "/", "/favicon.ico", "/index.html", "/static/**", "/.well-known/**",
            "/api/public/**", "/api/travel-posts/**", "/api/feeds/**", "/api/users/me",
            "/ws/**", "/ws-raw/**",
    };

    private static final String[] PUBLIC_POST_URLS = {
            "/oauth2/**", "/login", "/login/oauth2/code/**", "/error", "/api/v1/**"
    };

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FrontendProperties frontendProperties;

    /**
     * ✅ 1) OAuth2 전용 SecurityFilterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login", "/login/oauth2/code/**") // ✅ 이 경로에 대해서만 적용
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // ✅ 세션 허용
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        ;

        return http.build();
    }

    /**
     * ✅ 2) API 및 웹소켓 통합 SecurityFilterChain
     * - 역할: 웹소켓과 API 요청 모두 처리
     * - 순서: OAuth2 다음으로 모든 요청을 처리하므로 @Order(2)
     */
    @Bean
    @Order(2) // ❗️ 순서를 2로 변경하고, 기존 웹소켓 필터체인은 삭제합니다.
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**") // ✅ OAuth2를 제외한 모든 요청을 처리
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 👇 [핵심 최종 수정]
                .sessionManagement(session -> session
                        // 웹소켓과 API 모두 필요 시 세션을 사용하도록 허용
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 동시 요청으로 인한 세션 충돌(Session Invalidated 오류) 방지
                        .sessionFixation().none()
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_URLS).permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(frontendProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}