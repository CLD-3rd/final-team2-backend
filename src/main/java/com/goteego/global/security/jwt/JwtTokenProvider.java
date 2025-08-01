package com.goteego.global.security.jwt;

import com.goteego.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidityMs; // 10분

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidityMs; // 7일

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ✅ Access Token 생성
     *
     * @param user
     * @return
     */
    public String createAccessToken(User user) {
        return createToken(buildClaims(user), accessTokenValidityMs);
    }

    /**
     * ✅ Refresh Token 생성
     *
     * @param user
     * @return
     */
    public String createRefreshToken(User user) {
        return createToken(buildClaims(user), refreshTokenValidityMs);
    }

    /**
     * ✅ Refresh Token 기반으로 Access Token 재발급
     *
     * @param refreshToken
     * @return
     */
    public String createAccessTokenFromRefresh(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        return createToken(claims, accessTokenValidityMs);
    }

    /**
     * ✅ 토큰 유효성 검증
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * ✅ 토큰에서 userId 가져오기
     *
     * @param token
     * @return
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * ✅ 토큰에서 role/email 가져오기
     *
     * @param token
     * @return
     */
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * ✅ 토큰에서 role 가져오기
     *
     * @param token
     * @return
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * ✅ 만료 여부 확인
     *
     * @param token
     * @return
     */
    public boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /**
     * ✅ 초 단위로 Access Token 만료 시간 반환
     *
     * @return
     */
    public int getAccessTokenMaxAgeInSeconds() {
        return (int) (accessTokenValidityMs / 1000);
    }

    /**
     * ✅ 초 단위로 Refresh Token 만료 시간 반환
     *
     * @return
     */
    public int getRefreshTokenMaxAgeInSeconds() {
        return (int) (refreshTokenValidityMs / 1000);
    }

    /** ========================== 내부 유틸 ========================== */

    /**
     * 토큰 생성
     */
    private String createToken(Map<String, Object> claims, long validityMs) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Claims Map 생성
     */
    private Map<String, Object> buildClaims(User user) {
        return Map.of(
                Claims.SUBJECT, String.valueOf(user.getId()),
                "email", user.getOauthInfo().getOauthEmail(),
                "role", user.getRole().name()
        );
    }

    /**
     * Claims 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}