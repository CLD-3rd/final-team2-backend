package com.goteego.global.security;

import com.goteego.global.security.auth.OAuth2SuccessHandler;
import com.goteego.global.security.jwt.JwtAuthenticationFilter;
import com.goteego.global.web.FrontendProperties;
import com.goteego.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            "/ws/**",
    };

    private static final String[] PUBLIC_POST_URLS = {
            "/oauth2/**", "/login", "/error"
    };

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FrontendProperties frontendProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🛡️ [SecurityConfig] 기본 보안 설정 시작");
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 세션 사용하지 않음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_URLS).permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // ✅ 사용자 정보 처리
                        .successHandler(oAuth2SuccessHandler) // ✅ 성공 핸들러 등록
                )
                .logout(logout -> logout.logoutSuccessUrl("/")
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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