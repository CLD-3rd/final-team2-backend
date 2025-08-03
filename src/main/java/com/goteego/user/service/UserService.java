package com.goteego.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goteego.badge.domain.UserBadge;
import com.goteego.badge.dto.BadgeResponse;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.s3.S3Directory;
import com.goteego.global.s3.S3Service;
import com.goteego.global.security.jwt.RefreshTokenService;
import com.goteego.profileAnswer.dto.TravelTagResponse;
import com.goteego.profileAnswer.service.ProfileAnswerService;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.dto.UserProfileResponse;
import com.goteego.user.dto.UserResponse;
import com.goteego.user.dto.UserUpdateRequest;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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
    private final ProfileAnswerService profileAnswerService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenService refreshTokenService;
    private final S3Service s3Service;

    /**
     * 새로운 Refresh Token을 Redis에 저장합니다.
     * 기존 토큰은 덮어쓰기되며, TTL은 설정값을 따릅니다.
     *
     * @param userId   사용자 ID
     * @param newToken 새 Refresh Token
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

    /**
     * 사용자 프로필 정보를 조회하는 메서드
     *
     * <p>조회 내용:
     * <ul>
     *     <li>사용자 기본 정보 (닉네임, 프로필 이미지, 평점 등)</li>
     *     <li>사용자가 설정한 여행 태그 목록</li>
     *     <li>사용자가 보유한 전체 뱃지 목록</li>
     *     <li>사용자 프로필에 노출 중인 뱃지 목록</li>
     * </ul>
     *
     * @param userId 프로필을 조회할 사용자 ID
     * @return UserProfileResponse (사용자 프로필 정보)
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);

        // ✅ 사용자가 선택한 여행 태그 목록
        List<TravelTagResponse> travelTags = Optional.ofNullable(profileAnswerService.getUserTravelTags(userId))
                .orElse(Collections.emptyList());

        // ✅ 사용자가 보유한 UserBadge 목록
        List<UserBadge> userBadges = user.getBadges();

        // ✅ 프로필에 노출 중인 뱃지 (isDisplay = true)
        List<BadgeResponse> displayedBadges = userBadges.stream()
                .filter(UserBadge::isDisplay) // isDisplay == true
                .map(userBadge -> BadgeResponse.from(userBadge.getBadge()))
                .toList();

        // ✅ 사용자가 보유한 전체 뱃지
        List<BadgeResponse> ownedBadges = userBadges.stream()
                .map(userBadge -> BadgeResponse.from(userBadge.getBadge()))
                .toList();

        return UserProfileResponse.from(user, displayedBadges, ownedBadges, travelTags);
    }

    /**
     * ✅ 사용자 프로필 수정 서비스
     * <br>- 닉네임과 프로필 이미지를 동시에 수정 가능<br>
     * - 닉네임: 기존 값과 다를 경우에만 업데이트<br>
     * - 프로필 이미지:<br>
     * - 새 이미지가 존재하면 기존 이미지 삭제 후 업로드<br>
     * - 새 이미지가 없으면 기존 이미지 유지<br>
     * - 변경 후 캐시 무효화<br>
     *
     * @param userId  사용자 ID
     * @param request 닉네임과 프로필 이미지가 포함된 요청 DTO
     * @return UserResponse (수정된 사용자 정보)
     */
    @Transactional
    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        // ✅ 닉네임 업데이트 (기존과 다를 경우)
        if (!request.getNickname().equals(user.getNickname())) {
            user.updateNickname(request.getNickname());
        }

        // ✅ 프로필 이미지 업데이트
        MultipartFile image = request.getProfileImage();
        String updatedProfileImgUrl = user.getProfileImgUrl();

        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (updatedProfileImgUrl != null) {
                String oldKey = S3Service.extractKeyFromUrl(updatedProfileImgUrl, S3Directory.PROFILES);
                s3Service.deleteFile(oldKey);
            }

            // 새 이미지 업로드
            updatedProfileImgUrl = s3Service.uploadFile(image, S3Directory.PROFILES, user.getId());
            user.updateProfileImage(updatedProfileImgUrl);
        }

        // ✅ 캐시 무효화
        evictUserCache(userId);
        return UserResponse.from(user);
    }

    /**
     * ✅ 사용자 캐시 무효화
     *
     * @param userId 사용자 ID
     */
    private void evictUserCache(Long userId) {
        redisTemplate.delete(USER_CACHE_KEY + userId);
    }
}