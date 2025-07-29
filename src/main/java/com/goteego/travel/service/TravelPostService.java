package com.goteego.travel.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.domain.enumerate.PostType;

import com.goteego.travel.dto.travel.TravelPostCreateRequest;
import com.goteego.travel.dto.travel.BeforeTravelPostResponseDto;
import com.goteego.travel.dto.travel.NowTravelPostResponseDto;
import com.goteego.travel.dto.travel.TravelPostDetailResponseDto;
import com.goteego.travel.dto.travel.TravelPostResponseWrapper;
import com.goteego.travel.dto.travel.TravelPostUpdateRequest;
import com.goteego.travel.dto.PageResponseDto;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.recommendation.service.RecommendationService;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.goteego.user.domain.OauthInfo;
import com.goteego.user.domain.UserRole;

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
     */
    public TravelPostResponseWrapper getTravelPosts(PostType postType, int page, int size, Long currentUserId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPost> travelPostPage = travelPostRepository.findByPostTypeOrderByCreatedAtDescWithUser(postType, currentUserId, pageable);
        
        // PostType에 따라 다른 DTO 변환
        if (postType == PostType.BEFORE) {
            List<BeforeTravelPostResponseDto> content = convertToBeforeDtoList(travelPostPage.getContent(), currentUserId);
            return TravelPostResponseWrapper.before(content);
        } else {
            List<NowTravelPostResponseDto> content = convertToNowDtoList(travelPostPage.getContent(), currentUserId);
            return TravelPostResponseWrapper.now(content);
        }
    }
    










    /**
     * 여행 게시글 상세 조회
     */
    @Transactional
    public TravelPostDetailResponseDto getTravelPostDetail(Long postId) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 조회수 증가
        travelPost.incrementViewCount();
        return TravelPostDetailResponseDto.from(travelPost);
    }





    // =====================================================준형======================================================= //
    
    /**
     * 여행 게시글 생성
     */
    @Transactional
    public BeforeTravelPostResponseDto registerTravelPost(User user, TravelPostCreateRequest request) {

        // 채팅방 생성 및 저장 (ChatRoomService에게 책임 위임)
        ChatRoom groupChatRoom = chatRoomService.createGroupChatRoomForTravelPost(user, user.getNickname());

        // 게시글 생성
        TravelPost travelPost = TravelPost.builder()
                .user(user)
                .chatRoom(groupChatRoom)
                .title(request.getTitle())
                .content(request.getContent())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                .recruitLimit(request.getRecruitLimit())
                .postType(PostType.valueOf(request.getPostType().toUpperCase()))
                .isAddRecruit(request.getIsAddRecruit())
                .location(request.getLocationAsEnum())
                .build();

        // 게시글 저장
        TravelPost savedTravelPost = travelPostRepository.save(travelPost);

        return BeforeTravelPostResponseDto.from(savedTravelPost, user.getId(), user.getNickname(), 0.5, 0);
    }



    /**
     * 여행 게시글 참가 신청
     */
    @Transactional
    public ParticipationApplicationResponseDto joinTravelPost(Long travelPostId, User currentUser) {

        // 1. 여행 게시글 존재 확인
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 2. 참가 신청 검증
        validateJoinTravelPost(travelPost, currentUser);

        // 3. 참가 신청 생성
        ParticipationApplication application = ParticipationApplication.builder()
                .travelPost(travelPost)
                .user(currentUser)
                .status(ParticipationStatus.PENDING)
                .build();

        ParticipationApplication savedApplication = participationApplicationRepository.save(application);

        // 4. 사용자 정보 조회
        User user = getSafeUserInfo(currentUser.getId());

        log.info("참가 신청 생성 - travelPostId: {}, userId: {}, applicationId: {}", travelPostId, currentUser.getId(), savedApplication.getId());

        return ParticipationApplicationResponseDto.from(savedApplication, user);
    }




    // =====================================================준형======================================================= //















    // =====================================================재신======================================================= //

    /**
     * 여행 게시글 수정
     */
    @Transactional
    public TravelPost updateTravelPost(Long travelPostId, Long userId, TravelPostUpdateRequest request) {
        TravelPost travelPost = findTravelPostWithAuthorization(travelPostId, userId);
        
        // 게시글 수정 (도메인 객체의 비즈니스 로직 활용)
        travelPost.update(request);
        
        return travelPostRepository.save(travelPost);
    }

    /**
     * 여행 게시글 삭제
     */
    @Transactional
    public String deleteTravelPost(Long travelPostId, Long userId) {
        TravelPost travelPost = findTravelPostForDeletion(travelPostId, userId);
        
        // 참조 데이터 삭제
        deleteRelatedData(travelPostId);
        
        // 게시글 삭제
        travelPostRepository.delete(travelPost);
        
        return "게시글이 성공적으로 삭제되었습니다. (ID: " + travelPostId + ")";
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
     * N+1 문제 해결: BEFORE 타입 DTO 변환
     * - 모든 작성자 ID 수집
     * - 배치 유사도 계산
     * - DTO 변환
     */
    private List<BeforeTravelPostResponseDto> convertToBeforeDtoList(List<TravelPost> travelPosts, Long currentUserId) {
        if (travelPosts.isEmpty()) {
            return List.of();
        }
        
        // 모든 작성자 ID 수집
        List<Long> authorIds = travelPosts.stream()
                .map(tp -> tp.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
        
        // 한 번에 모든 유사도 계산
        Map<Long, Double> similarityMap = calculateSimilaritiesForUsers(currentUserId, authorIds);
        
        // DTO 변환
        return travelPosts.stream()
                .map(tp -> {
                    Long authorId = tp.getUser().getId();
                    String authorNickname = tp.getUser().getNickname();
                    Double similarity = similarityMap.getOrDefault(authorId, 0.5);
                    // 승인된 참가자 수 조회
                    Long approvedCount = participationApplicationRepository.countByTravelPostIdAndStatus(tp.getId(), ParticipationStatus.APPROVED);
                    Integer approvedParticipantCount = approvedCount != null ? approvedCount.intValue() : 0;
                    return BeforeTravelPostResponseDto.from(tp, currentUserId, authorNickname, similarity, approvedParticipantCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * N+1 문제 해결: NOW 타입 DTO 변환
     * - 모든 작성자 ID 수집
     * - 배치 유사도 계산
     * - DTO 변환
     */
    private List<NowTravelPostResponseDto> convertToNowDtoList(List<TravelPost> travelPosts, Long currentUserId) {
        if (travelPosts.isEmpty()) {
            return List.of();
        }
        
        // 모든 작성자 ID 수집
        List<Long> authorIds = travelPosts.stream()
                .map(tp -> tp.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
        
        // 한 번에 모든 유사도 계산
        Map<Long, Double> similarityMap = calculateSimilaritiesForUsers(currentUserId, authorIds);
        
        // DTO 변환
        return travelPosts.stream()
                .map(tp -> {
                    Long authorId = tp.getUser().getId();
                    String authorNickname = tp.getUser().getNickname();
                    Double similarity = similarityMap.getOrDefault(authorId, 0.5);
                    return NowTravelPostResponseDto.from(tp, currentUserId, authorNickname, similarity);
                })
                .collect(Collectors.toList());
    }

    /**
     * N+1 문제 해결: 여러 사용자에 대한 유사도를 한 번에 계산
     * - RecommendationService의 배치 메서드 활용
     * - 결과를 Map으로 변환
     */
    private Map<Long, Double> calculateSimilaritiesForUsers(Long currentUserId, List<Long> targetUserIds) {
        return recommendationService.calculateSimilaritiesForUsers(currentUserId, targetUserIds);
    }


    // =====================================================재신======================================================= //

}