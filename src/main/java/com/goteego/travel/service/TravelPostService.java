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
import com.goteego.travel.dto.travel.TravelPostResponseDto;
import com.goteego.travel.dto.travel.TravelPostUpdateRequest;
import com.goteego.travel.dto.PageResponseDto;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.recommendation.domain.UserEmbedding;
import com.goteego.recommendation.repository.UserEmbeddingRepository;
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
import java.util.Optional;
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
    private final UserEmbeddingRepository userEmbeddingRepository;
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    
    /**
     * 여행 게시글 목록 조회 (벡터 유사도 기반 정렬)
     * getTravelPosts -> convertToDto -> calculateUserSimilarity
     */
    public PageResponseDto<TravelPostResponseDto> getTravelPosts(PostType postType, int page, int size, Long currentUserId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPost> travelPostPage = travelPostRepository.findByPostTypeOrderByCreatedAtDescWithUser(postType, currentUserId, pageable);
        
        // N+1 문제 해결: 한 번에 모든 유사도 계산
        List<TravelPostResponseDto> content = convertToDtoList(travelPostPage.getContent(), currentUserId);
        
        return PageResponseDto.<TravelPostResponseDto>builder()
                .content(content)
                .pageable(PageResponseDto.PageableDto.builder()
                        .pageNumber(page)
                        .pageSize(size)
                        .build())
                .totalElements(travelPostPage.getTotalElements())
                .totalPages(travelPostPage.getTotalPages())
                .build();
    }
    










    /**
     * 여행 게시글 상세 조회
     */
    @Transactional
    public TravelPost getTravelPostDetail(Long postId) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 조회수 증가
        travelPost.incrementViewCount();
        return travelPost;
    }





    // =====================================================준형======================================================= //
    
    /**
     * 여행 게시글 생성
     */
    @Transactional
    public TravelPostResponseDto registerTravelPost(User user, TravelPostCreateRequest request) {

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
                .build();

        // 게시글 저장
        TravelPost savedTravelPost = travelPostRepository.save(travelPost);

        return TravelPostResponseDto.from(savedTravelPost, user.getId(), user.getNickname(), 0.5);
    }



    /**
     * 여행 게시글 참가 신청
     */
    @Transactional
    public ParticipationApplicationResponseDto joinTravelPost(Long travelPostId, User currentUser) {

        // 1. 여행 게시글 존재 확인
        TravelPost travelPost = getTravelPostDetail(travelPostId);

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
     * - 사용자 리뷰 데이터 삭제
     */
    private void deleteRelatedData(Long travelPostId) {
        travelPostRepository.deleteParticipationApplications(travelPostId);
        travelPostRepository.deleteUserReviews(travelPostId);
    }

    /**
     * N+1 문제 해결: 리스트 전체를 한 번에 처리
     * - 모든 작성자 ID 수집
     * - 배치 유사도 계산
     * - DTO 변환
     */
    private List<TravelPostResponseDto> convertToDtoList(List<TravelPost> travelPosts, Long currentUserId) {
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
                    return TravelPostResponseDto.from(tp, currentUserId, authorNickname, similarity);
                })
                .collect(Collectors.toList());
    }

    /**
     * N+1 문제 해결: 여러 사용자에 대한 유사도를 한 번에 계산
     * - 현재 사용자 임베딩 조회
     * - 배치 유사도 계산
     * - 결과를 Map으로 변환
     */
    private Map<Long, Double> calculateSimilaritiesForUsers(Long currentUserId, List<Long> targetUserIds) {
        if (currentUserId == null || targetUserIds.isEmpty()) {
            return Map.of();
        }
        
        try {
            log.debug("=== 다중 유사도 계산 시작 ===");
            log.debug("currentUserId: {}, targetUserIds: {}", currentUserId, targetUserIds);
            
            // 현재 사용자의 임베딩 조회
            Optional<UserEmbedding> currentUserEmbedding = userEmbeddingRepository.findByUserId(currentUserId);
            
            if (currentUserEmbedding.isEmpty()) {
                log.warn("현재 사용자 임베딩이 없어서 기본값 0.5 반환");
                return targetUserIds.stream().collect(Collectors.toMap(id -> id, id -> 0.5));
            }

            // 한 번의 쿼리로 모든 유사도 계산
            List<Object[]> similarities = userEmbeddingRepository.calculateAllSimilarities(currentUserEmbedding.get().getUserEmbedding(), currentUserId);
            
            // 결과를 Map으로 변환
            Map<Long, Double> similarityMap = similarities.stream()
                    .filter(result -> targetUserIds.contains((Long) result[0]))
                    .collect(Collectors.toMap(
                            result -> (Long) result[0],
                            result -> (Double) result[2]
                    ));
            
            // 누락된 사용자들에 대해 기본값 설정
            targetUserIds.forEach(id -> similarityMap.putIfAbsent(id, 0.5));
            
            log.debug("계산된 유사도 맵: {}", similarityMap);
            return similarityMap;
            
        } catch (Exception e) {
            log.error("다중 유사도 계산 중 에러 발생: {}", e.getMessage(), e);
            return targetUserIds.stream().collect(Collectors.toMap(id -> id, id -> 0.5));
        }
    }


    // =====================================================재신======================================================= //

}