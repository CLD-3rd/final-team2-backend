package com.goteego.travel.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.dto.PageInfo;
import com.goteego.global.dto.SearchCondition;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.error.exception.UnauthorizedAccessException;
import com.goteego.recommendation.service.RecommendationService;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.travel.*;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * 여행 게시글 서비스
 * 여행 게시글의 비즈니스 로직을 담당하는 서비스 클래스
 * 게시글 CRUD, 참가자 관리, 일정 관리 등의 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPostService {

    private final TravelPostRepository travelPostRepository;
    private final ParticipationApplicationRepository participationApplicationRepository;
    private final RecommendationService recommendationService;
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 여행 게시글 목록 조회
     * - PostType(BEFORE / NOW)에 따라 다른 DTO 변환
     * - 검색 조건(제목, 작성자, 지역) 및 정렬 조건(sort) 적용
     * - 로그인 사용자 기반 similarity 정렬 처리
     *
     * @param postType  게시글 타입 (BEFORE / NOW)
     * @param page      페이지 번호
     * @param size      페이지 크기
     * @param condition 검색 및 정렬 조건
     * @param user      로그인 사용자 정보 (null 허용)
     * @return 게시글 목록 + 페이지 정보
     */
    @Transactional(readOnly = true)
    public TravelPostResponseWrapper getTravelPosts(PostType postType,
                                                    int page,
                                                    int size,
                                                    SearchCondition condition,
                                                    User user) {
        // ✅ 로그인 여부 확인
        Long currentUserId = (user != null) ? user.getId() : null;

        // ✅ 정렬 조건
        Pageable pageable = PageRequest.of(page, size, getSortOption(postType, condition.sort()));

        // ✅ location 검증 및 변환
        Location locationEnum = null;
        if (condition.location() != null && !condition.location().isBlank()) {
            try {
                locationEnum = Location.fromDisplayName(condition.location());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_LOCATION);
            }
        }

        // ✅ Repository 호출 (검색 조건 적용)
        Page<TravelPost> travelPostPage = travelPostRepository.getTravelPostsWithCondition(
                postType,
                condition.title(),
                condition.author(),
                locationEnum,
                pageable
        );
        PageInfo pageInfo = PageInfo.from(travelPostPage);

        // ✅ PostType에 따라 다른 DTO 변환
        if (postType == PostType.BEFORE) {
            List<BeforeTravelPostResponseDto> content = convertToDtoList(
                    travelPostPage.getContent(),
                    currentUserId,
                    ctx -> {
                        Long approvedCount = participationApplicationRepository
                                .countByTravelPostIdAndStatus(ctx.tp().getId(), ParticipationStatus.APPROVED);
                        int approvedParticipantCount = approvedCount != null ? approvedCount.intValue() : 0;
                        return BeforeTravelPostResponseDto.from(ctx.tp(), ctx.nickname(), approvedParticipantCount, ctx.viewCount());
                    }
            );
            return TravelPostResponseWrapper.before(content, pageInfo);

        } else if (postType == PostType.NOW) {
            List<NowTravelPostResponseDto> content = convertToDtoList(
                    travelPostPage.getContent(),
                    currentUserId,
                    ctx -> NowTravelPostResponseDto.from(ctx.tp(), ctx.nickname())
            );
            return TravelPostResponseWrapper.now(content, pageInfo);
        }

        throw new BusinessException(ErrorCode.UNSUPPORTED_POST_TYPE);
    }

    /**
     * 여행 게시글 상세 조회
     */
    @Transactional
    public TravelPostDetailResponseDto getTravelPostDetail(Long postId) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // [1] Redis에서 조회수 증가
        String viewCountKey = "post:view:" + postId;
        Long viewCount = redisTemplate.opsForValue().increment(viewCountKey);

        // [2] 30번 조회할 때 마다 DB 반영
        if (viewCount % 30 == 0) { //
            travelPost.updateViewCount(viewCount);
            travelPostRepository.save(travelPost);
        }

        return TravelPostDetailResponseDto.from(travelPost);
    }

    /**
     * 여행 게시글 생성
     */
    @Transactional
    public Long createTravelPost(PostType postType, Long userId, TravelPostRequest request) {

        User currentUser = getValidatedUser(userId);

        TravelPost createdTravelPost;

        if (postType == PostType.BEFORE) {
            ChatRoom groupChatRoom = chatRoomService.createGroupChatRoomForTravelPost(currentUser, currentUser.getNickname());
            String imageUrl = uploadImage(request.getImage());

            createdTravelPost = TravelPost.builder()
                    .user(currentUser)
                    .title(request.getTitle())
                    .location(request.getLocationAsEnum())
                    .postType(postType)
                    .chatRoom(groupChatRoom)
                    .content(request.getContent())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .imageUrl(imageUrl)
                    .recruitLimit(request.getRecruitLimit())
                    .isAddRecruit(request.getIsAddRecruit())
                    .build();
        } else if (postType == PostType.NOW) {
            createdTravelPost = TravelPost.builder()
                    .user(currentUser)
                    .title(request.getTitle())
                    .location(request.getLocationAsEnum())
                    .postType(postType)
                    .build();
        } else {
            throw new BusinessException(ErrorCode.UNSUPPORTED_POST_TYPE);
        }

        TravelPost savedTravelPost = travelPostRepository.save(createdTravelPost);
        return savedTravelPost.getId();
    }

    /**
     * 여행 게시글 수정
     */
    @Transactional
    public void updateTravelPost(Long travelPostId, Long userId, TravelPostRequest request) {

        TravelPost travelPost = findTravelPostWithAuthorization(travelPostId, userId);

        String imageUrl = null;
        if (travelPost.getPostType() == PostType.BEFORE) {
            imageUrl = uploadImage(request.getImage());
        }
        // 게시글 수정 (도메인 객체의 비즈니스 로직 활용)
        travelPost.update(travelPost.getPostType(), request, imageUrl);
    }

    /**
     * 여행 게시글 삭제
     * <p>
     * - 게시글을 삭제하기 전에 다음 조건을 검증:<br>
     * 1. 게시글이 존재하는지 확인<br>
     * 2. 요청자가 게시글 작성자인지 권한 검증<br>
     * 3. 해당 게시글에 승인된 참여자(approved)가 없는지 확인
     * <p>
     * 모든 조건을 만족하면 게시글을 삭제합니다.
     *
     * @param travelPostId 삭제할 게시글 ID
     * @param userId       요청한 사용자 ID
     */
    @Transactional
    public void deleteTravelPost(Long travelPostId, Long userId) {
        // 1. 게시글 조회
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 2. 작성자 권한 검증
        if (!travelPost.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(ErrorCode.UNAUTHORIZED_POST_DELETE);
        }

        // 3. 승인된 참여자 존재 여부 확인
        boolean hasApprovedParticipants = participationApplicationRepository.existsByTravelPostIdAndStatus(travelPostId, ParticipationStatus.APPROVED);
        if (hasApprovedParticipants) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_TRAVEL_POST_WITH_APPROVED_PARTICIPANTS);
        }

        // 4. 게시글 삭제
        travelPostRepository.delete(travelPost);
    }

    /**
     * 여행 게시글 참가 신청
     */
    @Transactional
    public void joinTravelPost(Long travelPostId, Long userId) {

        // 1. 사용자 정보 조회
        User currentUser = getValidatedUser(userId);

        // 2. 여행 게시글 존재 확인
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 3. 참가 신청 검증
        validateJoinTravelPost(travelPost, currentUser);

        // 4. 참가 신청 생성
        ParticipationApplication application = ParticipationApplication.builder()
                .travelPost(travelPost)
                .user(currentUser)
                .status(ParticipationStatus.PENDING)
                .build();

        participationApplicationRepository.save(application);

        log.info("참가 신청 생성 - travelPostId: {}, userId: {}", travelPostId, currentUser.getId());
    }

    // =====================================================내부 로직======================================================= //

    /**
     * 참가 신청 검증 로직
     * - 자기 자신의 게시글 신청 방지
     * - 중복 신청 방지
     * - 모집 마감 여부 확인
     */
    private void validateJoinTravelPost(TravelPost travelPost, User currentUser) {
        Long travelPostId = travelPost.getId();
        Long currentUserId = currentUser.getId();

        // 자기 자신의 게시글에는 신청 불가
        if (travelPost.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.SELF_APPLICATION_NOT_ALLOWED);
        }

        // 중복 신청 방지
        if (participationApplicationRepository.existsByTravelPostIdAndUserId(travelPostId, currentUserId)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        // 모집 마감 여부 확인
        Long approvedCount = participationApplicationRepository.countByTravelPostIdAndStatus(travelPostId, ParticipationStatus.APPROVED);
        if (approvedCount >= travelPost.getRecruitLimit()) {
            // 모집 완료 상태로 업데이트
            travelPost.updateRecruitStatus(false);
            travelPostRepository.save(travelPost);
            throw new BusinessException(ErrorCode.RECRUITMENT_FULL);
        }
    }

    /**
     * 안전한 사용자 정보 조회
     * - 조회 실패 시 기본 사용자 객체 반환
     */
    private User getValidatedUser(Long userId) {
        return userService.getUserById(userId);
    }

    /**
     * 게시글 정렬 옵션 처리
     * - BEFORE: recent(최신순), view(조회수순)
     * - NOW: 항상 최신순
     *
     * @param postType 게시글 타입 (BEFORE / NOW)
     * @param sort     정렬 기준 (recent / view), 기본값: recent
     * @return Sort 객체
     */
    private Sort getSortOption(PostType postType, String sort) {
        // 기본 정렬 기준
        String sortKey = (sort == null || sort.isBlank()) ? "recent" : sort.toLowerCase();

        return switch (postType) {
            case BEFORE -> switch (sortKey) {
                case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
                default -> Sort.by(Sort.Direction.DESC, "viewCount");
            };
            case NOW -> Sort.by(Sort.Direction.DESC, "createdAt"); // NOW는 항상 최신순
        };
    }

    /**
     * 수정 권한 확인 및 게시글 조회
     * - 게시글 존재 여부 확인
     * - 작성자 권한 확인
     */
    private TravelPost findTravelPostWithAuthorization(Long travelPostId, Long userId) {
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 권한 확인 - 작성자만 수정 가능
        if (!travelPost.isAuthor(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_UPDATE);
        }

        return travelPost;
    }

    /**
     * TravelPost 리스트를 DTO 리스트로 변환합니다.
     * <p>N+1 문제를 방지하기 위해 작성자 ID를 한 번에 수집하고,
     * 로그인 사용자가 있을 경우 유사도를 배치 계산한 뒤 DTO를 생성합니다.</p>
     *
     * @param travelPosts
     * @param currentUserId
     * @param converter
     * @param <T>
     * @return
     */
    private <T> List<T> convertToDtoList(
            List<TravelPost> travelPosts,
            Long currentUserId,
            Function<TravelPostContext, T> converter
    ) {
        if (travelPosts.isEmpty()) return List.of();

        List<TravelPost> sortedPosts;

        if (currentUserId != null) {
            // 작성자 ID 수집
            List<Long> authorIds = travelPosts.stream()
                    .map(tp -> tp.getUser().getId())
                    .distinct()
                    .toList();

            // 유사도 계산
            Map<Long, Double> similarityMap = calculateSimilaritiesForUsers(currentUserId, authorIds);

            // similarity 내림차순 정렬
            sortedPosts = travelPosts.stream()
                    .sorted((tp1, tp2) -> {
                        double sim1 = similarityMap.getOrDefault(tp1.getUser().getId(), 0.0);
                        double sim2 = similarityMap.getOrDefault(tp2.getUser().getId(), 0.0);
                        return Double.compare(sim2, sim1); // 높은 similarity 먼저
                    })
                    .toList();
        } else {
            // 비로그인 사용자는 정렬 불필요 → 원본 그대로
            sortedPosts = travelPosts;
        }

        // DTO 변환
        return sortedPosts.stream()
                .map(tp -> {
                    // Redis 조회수 확인
                    Long redisViewCounts = getViewCountFromRedis(tp.getId());
                    TravelPostContext travelPostContext = new TravelPostContext(tp, currentUserId, tp.getUser().getNickname(),
                                                                    redisViewCounts != null ? redisViewCounts : tp.getViewCount());
                    return converter.apply(travelPostContext);
                })
                .toList();
    }

    private Long getViewCountFromRedis(Long postId) {
        try {
            String key = "post:view:" + postId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value.toString()) : null;
        } catch (Exception e) {
            log.warn("Redis 조회수 조회 실패 postId: {}", postId, e);
            return null;
        }
    }

    /**
     * N+1 문제 해결: 여러 사용자에 대한 유사도를 한 번에 계산
     * - RecommendationService의 배치 메서드 활용
     * - 결과를 Map으로 변환
     */
    private Map<Long, Double> calculateSimilaritiesForUsers(Long currentUserId, List<Long> targetUserIds) {
        return recommendationService.calculateSimilaritiesForUsers(currentUserId, targetUserIds);
    }

    /**
     * 이미지 업로드 로직 분리
     */
    private String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null; // 기본 이미지 URL 또는 null
        }
        // 실제 S3 업로드 로직 호출 (ImageService)
//        S3Service.upload(image);
        return "/test/image";
    }
}