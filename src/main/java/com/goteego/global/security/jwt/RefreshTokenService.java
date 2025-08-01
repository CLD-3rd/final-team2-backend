package com.goteego.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofSeconds(jwtTokenProvider.getRefreshTokenMaxAgeInSeconds())
        );
    }

    public Optional<String> getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
}