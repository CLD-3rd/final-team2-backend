package com.goteego.travel.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.chat.service.ChatService;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.travel.TravelPostCreateRequest;
import com.goteego.travel.dto.travel.TravelPostResponseDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        Page<TravelPost> travelPostPage = travelPostRepository.findByPostTypeOrderByCreatedAtDesc(postType, currentUserId, pageable);
        
        // 실제 유사도 계산을 포함한 DTO 변환
        List<TravelPostResponseDto> content = travelPostPage.getContent().stream()
                .map(travelPost -> convertToDto(travelPost, currentUserId))
                .collect(Collectors.toList());
        
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
    
    // 내부 메서드 -> TravelPost를 DTO로 변환 (유사도 계산 포함)
    private TravelPostResponseDto convertToDto(TravelPost travelPost, Long currentUserId) {
        // 실제 사용자 정보 조회 (예외 처리 추가)

        User author = userService.getUserById(travelPost.getUser().getId());
        Long authorId = author.getId();
        String authorNickname = author.getNickname();


        // 실제 벡터 유사도 계산
        Double similarity = calculateUserSimilarity(currentUserId, authorId);

        // 반환용 DTO 변환
        return TravelPostResponseDto.from(travelPost, currentUserId, authorNickname, similarity);
    }


    // 내부 메서드 -> 두 사용자 간의 벡터 유사도 계산
    private Double calculateUserSimilarity(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || currentUserId.equals(targetUserId)) {
            return 1.0; // 자기 자신과의 유사도는 1.0
        }
        
        try {
            log.debug("=== 유사도 계산 시작 ===");
            log.debug("currentUserId: {}, targetUserId: {}", currentUserId, targetUserId);
            
            // 현재 사용자의 임베딩 조회
            Optional<UserEmbedding> currentUserEmbedding = userEmbeddingRepository.findByUserId(currentUserId);
            
            if (currentUserEmbedding.isEmpty()) {
                log.warn("현재 사용자 임베딩이 없어서 기본값 0.5 반환");
                return 0.5;
            }


            // 한 번의 쿼리로 모든 유사도 계산
            List<Object[]> similarities = userEmbeddingRepository.calculateAllSimilarities(currentUserEmbedding.get().getUserEmbedding(), currentUserId);
            
            // targetUserId에 해당하는 유사도 찾기
            for (Object[] result : similarities) {
                Long userId = (Long) result[0];
                Double distance = (Double) result[1];
                Double similarity = (Double) result[2];
                
                if (userId.equals(targetUserId)) {
                    log.debug("찾은 유사도 - userId: {}, distance: {}, similarity: {}", userId, distance, similarity);
                    return similarity;
                }
            }
            
            log.warn("대상 사용자 임베딩이 없어서 기본값 0.5 반환");
            return 0.5;
            
        } catch (Exception e) {
            log.error("유사도 계산 중 에러 발생: {}", e.getMessage(), e);
            return 0.5;
        }
    }


    /**
     * 여행 게시글 상세 조회
     */
    @Transactional
    public TravelPost getTravelPostDetail(Long postId) {

        TravelPost travelPost = travelPostRepository.findById(postId).orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

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
                .recruitLimit(request.getRecuitLimit())
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

    // 내부 로직
    private void validateJoinTravelPost(TravelPost travelPost, User currentUser) {
        Long travelPostId = travelPost.getId();
        Long currentUserId = currentUser.getId();

        // 자기 자신의 게시글에는 신청 불가
        if (travelPost.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("자신의 게시글에는 참가 신청할 수 없습니다.");
        }

        // 중복 신청 방지
        if (participationApplicationRepository.existsByTravelPostIdAndUserId(travelPostId, currentUserId)) {
            throw new RuntimeException("이미 참가 신청한 게시글입니다.");
        }

        // 모집 마감 여부 확인
        Long approvedCount = participationApplicationRepository.countByTravelPostIdAndStatus(travelPostId, ParticipationStatus.APPROVED);
        if (approvedCount >= travelPost.getRecruitLimit()) {
            throw new RuntimeException("모집 인원이 마감되었습니다.");
        }
    }

    // 내부 로직
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


    // =====================================================준형======================================================= //















    // =====================================================재신======================================================= //

    /**
     * 여행 게시글 수정
     */
    @Transactional
    public TravelPost updateTravelPost(Long travelPostId, Long userId, String title, String content,
                                       LocalDate startTime, LocalDate endTime, String imageUrl,
                                       Integer recuitLimit, PostType postType, Boolean isAddRecruit) {

        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(travelPostId);
        
        if (travelPostOpt.isEmpty()) {
            throw new RuntimeException("Travel post not found with id: " + travelPostId);
        }
        
        TravelPost travelPost = travelPostOpt.get();
        
        // 권한 확인 - 작성자만 수정 가능
        if (!travelPost.isAuthor(userId)) {
            throw new RuntimeException("Only the author can update the travel post");
        }
        
        // 게시글 수정
        travelPost.update(title, content, startTime, endTime, imageUrl, recuitLimit, postType, isAddRecruit);
        
        return travelPostRepository.save(travelPost);
    }
    
    /**
     * 여행 게시글 삭제
     */
    @Transactional
    public String deleteTravelPost(Long travelPostId, Long userId) {
        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(travelPostId);
        
        if (travelPostOpt.isEmpty()) {
            throw new RuntimeException("Travel post not found with id: " + travelPostId);
        }
        
        TravelPost travelPost = travelPostOpt.get();
        
        // 권한 확인 - 작성자만 삭제 가능
        if (!travelPost.isAuthor(userId)) {
            throw new RuntimeException("Only the author can delete the travel post");
        }
        
        // 참조 데이터 삭제
        travelPostRepository.deleteParticipationApplications(travelPostId);
        travelPostRepository.deleteUserReviews(travelPostId);
        
        // 게시글 삭제
        travelPostRepository.delete(travelPost);
        
        return "게시글이 성공적으로 삭제되었습니다. (ID: " + travelPostId + ")";
    }


    // =====================================================재신======================================================= //

}