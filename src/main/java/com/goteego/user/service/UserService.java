package com.goteego.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.jwt.RefreshTokenService;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private static final String USER_CACHE_KEY = "user:";
    private static final long CACHE_TTL = 3600; // 3600초
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenService refreshTokenService;

    /**
     * 사용자에게 새로운 Refresh Token을 발급하고 Redis에 저장합니다.<br>
     * 기존 Refresh Token은 덮어씌워지며, TTL(유효기간)은 JwtTokenProvider에서 설정한 값이 적용됩니다
     *
     * @param userId
     * @param newToken
     */
    @Transactional
    public void updateRefreshToken(Long userId, String newToken) {
        refreshTokenService.saveRefreshToken(userId, newToken);
    }

    /**
     * 사용자 정보 조회
     */
    // [1] 일반 RDBMS 버전
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // [2] Redis 적용 버전
    public UserDto getUser(Long userId) {
        String key = USER_CACHE_KEY + userId;

        Object cachedObj = redisTemplate.opsForValue().get(key);
        if (cachedObj instanceof UserDto) {
            log.info("Cache hit for user: {}", userId);
            return (UserDto) cachedObj;
        } else if (cachedObj instanceof LinkedHashMap) {
            UserDto userDto = objectMapper.convertValue(cachedObj, UserDto.class);
            log.info("Cache hit (converted) for user: {}", userId);
            return userDto;
        } else if (cachedObj != null) {
            log.warn("Unexpected type in cache for user: {}, type: {}", userId, cachedObj.getClass());
            throw new IllegalStateException("Unexpected cache type for user: " + userId);
        }

        log.info("Cache miss for user: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        UserDto userDto = new UserDto(user.getId(), user.getNickname(), user.getOauthInfo().getOauthEmail());
        redisTemplate.opsForValue().set(key, userDto, CACHE_TTL, TimeUnit.SECONDS);
        return userDto;

    }

    public List<UserDto> findAllExcludingMe(Long excludeId) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(excludeId)) // 자신 제외
                .map(user -> new UserDto(user.getId(), user.getNickname(), user.getOauthInfo().getOauthEmail()))
                .collect(Collectors.toList());
    }

    // 현재 구조에서는 유저 수정이 없음. 만약 추가한다면 아래 코드 사용
    public User updateUser(Long userId, User updatedUser) {
        // DB 업데이트
        User savedUser = userRepository.save(updatedUser);

        // 캐시 무효화
        String key = USER_CACHE_KEY + userId;
        redisTemplate.delete(key);

        return savedUser;
    }
}