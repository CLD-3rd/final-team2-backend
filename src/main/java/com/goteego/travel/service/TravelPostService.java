package com.goteego.travel.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.global.dto.PageInfo;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.recommendation.service.RecommendationService;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.travel.*;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.User;
import com.goteego.user.domain.UserRole;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    /**
     * 여행 게시글 목록 조회 (PostType별 다른 응답 구조)
     *
     * @param postType
     * @param page
     * @param size
     * @param user
     * @return
     */
    public TravelPostResponseWrapper getTravelPosts(PostType postType, int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPost> travelPostPage = travelPostRepository.findByPostTypeOrderByCreatedAtDesc(postType, pageable);
        PageInfo pageInfo = PageInfo.from(travelPostPage);

        // 로그인 여부 확인
        Long currentUserId = (user != null) ? user.getId() : null;

        // PostType에 따라 다른 DTO 변환
        if (postType == PostType.BEFORE) {
            List<BeforeTravelPostResponseDto> content = convertToDtoList(
                    travelPostPage.getContent(),
                    currentUserId,
                    ctx -> {
                        Long approvedCount = participationApplicationRepository
                                .countByTravelPostIdAndStatus(ctx.tp().getId(), ParticipationStatus.APPROVED);
                        int approvedParticipantCount = approvedCount != null ? approvedCount.intValue() : 0;
                        return BeforeTravelPostResponseDto.from(ctx.tp(), ctx.nickname(), ctx.similarity(), approvedParticipantCount);
                    }
            );
            return TravelPostResponseWrapper.before(content, pageInfo);
        } else if (postType == PostType.NOW) {
            List<NowTravelPostResponseDto> content = convertToDtoList(
                    travelPostPage.getContent(),
                    currentUserId,
                    ctx -> NowTravelPostResponseDto.from(ctx.tp(), ctx.currentUserId(), ctx.nickname(), ctx.similarity())
            );
            return TravelPostResponseWrapper.now(content, pageInfo);
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_POST_TYPE);
    }

    /**
     * 여행 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public TravelPostDetailResponseDto getTravelPostDetail(Long postId) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 조회수 증가
        travelPost.incrementViewCount();
        return TravelPostDetailResponseDto.from(travelPost);
    }

    /**
     * 여행 게시글 생성
     */
    @Transactional
    public Long registerTravelPost(User user, TravelPostCreateRequest request) {
        // 채팅방 생성 및 저장 (ChatRoomService에게 책임 위임)
        ChatRoom groupChatRoom = chatRoomService.createGroupChatRoomForTravelPost(user, user.getNickname());

        MultipartFile userUploadedImage = request.getImage();
        // TODO: + 이미지 S3에 업로드 후, imageUrl return 로직 추가
        String imageUrl = "https://aws.com";

        // 게시글 생성
        TravelPost travelPost = TravelPost.builder()
                .user(user)
                .chatRoom(groupChatRoom)
                .title(request.getTitle())
                .content(request.getContent())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(imageUrl)
                .recruitLimit(request.getRecruitLimit())
                .postType(PostType.valueOf(request.getPostType().toUpperCase()))
                .isAddRecruit(request.getIsAddRecruit())
                .location(request.getLocationAsEnum())
                .build();

        // 게시글 저장
        TravelPost savedTravelPost = travelPostRepository.save(travelPost);
        return savedTravelPost.getId();
    }

    /**
     * 여행 게시글 수정
     */
    @Transactional
    public void updateTravelPost(Long travelPostId, Long userId, TravelPostUpdateRequest request) {
        TravelPost travelPost = findTravelPostWithAuthorization(travelPostId, userId);

        MultipartFile userUploadedImage = request.getImage();
        // TODO: + 이미지 S3에 업로드 후, imageUrl return 로직 추가
        String imageUrl = "https://aws-update.com";

        // 게시글 수정 (도메인 객체의 비즈니스 로직 활용)
        travelPost.update(request, imageUrl);
    }

    /**
     * 여행 게시글 삭제
     */
    @Transactional
    public void deleteTravelPost(Long travelPostId, Long userId) {
        TravelPost travelPost = findTravelPostForDeletion(travelPostId, userId);

        // 참조 데이터 삭제
        deleteRelatedData(travelPostId);

        // 게시글 삭제
        travelPostRepository.delete(travelPost);
    }

    /**
     * 여행 게시글 참가 신청
     */
    @Transactional
    public void joinTravelPost(Long travelPostId, User currentUser) {

        // 1. 사용자 정보 조회
        User user = getSafeUserInfo(currentUser.getId());

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
    private User getSafeUserInfo(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return User.builder()
                    .nickname("알 수 없는 사용자")
                    .profileImgUrl("")
                    .role(UserRole.USER)
                    .oauthInfo(OauthInfo.builder().build())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
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
     * 삭제 권한 확인 및 게시글 조회
     * - 게시글 존재 여부 확인
     * - 작성자 권한 확인
     * - 삭제 가능 여부 확인
     */
    private TravelPost findTravelPostForDeletion(Long travelPostId, Long userId) {
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 권한 확인 - 작성자만 삭제 가능
        if (!travelPost.isAuthor(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_DELETE);
        }

        // 삭제 가능 여부 확인 (도메인 로직 활용)
        if (!travelPost.canBeDeleted()) {
            throw new BusinessException(ErrorCode.INVALID_POST_DATA);
        }

        return travelPost;
    }

    /**
     * 관련 데이터 삭제
     * - 참가 신청 데이터 삭제
     */
    private void deleteRelatedData(Long travelPostId) {
        travelPostRepository.deleteParticipationApplications(travelPostId);
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

        // 작성자 ID 수집
        List<Long> authorIds = travelPosts.stream()
                .map(tp -> tp.getUser().getId())
                .distinct()
                .toList();

        // 유사도 계산
        Map<Long, Double> similarityMap = (currentUserId != null)
                ? calculateSimilaritiesForUsers(currentUserId, authorIds)
                : Collections.emptyMap();

        return travelPosts.stream()
                .map(tp -> {
                    Long authorId = tp.getUser().getId();
                    String nickname = tp.getUser().getNickname();
                    Double similarity = similarityMap.getOrDefault(authorId, 0.5);
                    return converter.apply(new TravelPostContext(tp, currentUserId, nickname, similarity));
                })
                .toList();
    }

    /**
     * N+1 문제 해결: 여러 사용자에 대한 유사도를 한 번에 계산
     * - RecommendationService의 배치 메서드 활용
     * - 결과를 Map으로 변환
     */
    private Map<Long, Double> calculateSimilaritiesForUsers(Long currentUserId, List<Long> targetUserIds) {
        return recommendationService.calculateSimilaritiesForUsers(currentUserId, targetUserIds);
    }
}