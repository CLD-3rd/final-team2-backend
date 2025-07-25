package com.goteego.travel.service;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.dto.TravelPostResponseDto;
import com.goteego.travel.dto.PageResponseDto;
import com.goteego.travel.dto.ParticipationApplicationResponseDto;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.recommendation.domain.UserEmbedding;
import com.goteego.recommendation.repository.UserEmbeddingRepository;
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
    private final RecommendationService recommendationService;
    private final UserService userService;
    
    /**
     * 여행 게시글 목록 조회 (벡터 유사도 기반 정렬)
     * 
     * @param postType 게시글 타입 (BEFORE/NOW)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 페이징된 여행 게시글 목록 (유사도 점수 포함)
     */
    public PageResponseDto<TravelPostResponseDto> getTravelPosts(TravelPost.PostType postType, int page, int size, Long currentUserId) {
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
    
    /**
     * TravelPost를 DTO로 변환 (유사도 계산 포함)
     */
    private TravelPostResponseDto convertToDto(TravelPost travelPost, Long currentUserId) {
        // 실제 사용자 정보 조회 (예외 처리 추가)
        String nickname;
        try {
            User user = userService.getUserById(travelPost.getUserId());
            nickname = user.getNickname();
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패 - userId: {}, error: {}", travelPost.getUserId(), e.getMessage());
            nickname = "알 수 없는 사용자";
        }
        
        // 실제 벡터 유사도 계산
        Double similarity = calculateUserSimilarity(currentUserId, travelPost.getUserId());
        
        return TravelPostResponseDto.from(travelPost, currentUserId, nickname, similarity);
    }
    
    /**
     * 두 사용자 간의 벡터 유사도 계산
     * 
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param targetUserId 대상 사용자 ID (게시글 작성자)
     * @return 유사도 점수 (0~1 사이, 높을수록 유사함)
     */
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
            List<Object[]> similarities = userEmbeddingRepository.calculateAllSimilarities(
                currentUserEmbedding.get().getUserEmbedding(),
                currentUserId
            );
            
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
     * 
     * @param postId 게시글 ID
     * @return 여행 게시글 상세 정보
     * @throws RuntimeException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public TravelPost getTravelPostDetail(Long postId) {
        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(postId);
        
        if (travelPostOpt.isEmpty()) {
            throw new RuntimeException("Travel post not found with id: " + postId);
        }
        
        TravelPost travelPost = travelPostOpt.get();
        
        // 조회수 증가
        travelPost.incrementViewCount();
        travelPostRepository.incrementViewCount(postId);
        
        return travelPost;
    }
    
    /**
     * 여행 게시글 생성
     * 
     * @param userId 작성자 ID
     * @param title 제목
     * @param content 내용
     * @param startTime 시작일
     * @param endTime 종료일
     * @param imageUrl 이미지 URL
     * @param recuitLimit 모집 인원
     * @param postType 게시글 타입
     * @param isAddRecruit 추가 모집 여부
     * @return 생성된 여행 게시글
     */
    @Transactional
    public TravelPost createTravelPost(Long userId, String title, String content, 
                                     LocalDate startTime, LocalDate endTime, String imageUrl, 
                                     Integer recuitLimit, TravelPost.PostType postType, Boolean isAddRecruit) {
        
        // 새로운 채팅방 ID 생성 (Mock 데이터)
        Long newChatRoomId = generateNewChatRoomId();
        
        TravelPost travelPost = TravelPost.builder()
                .userId(userId)
                .chatRoomId(newChatRoomId)
                .title(title)
                .content(content)
                .startTime(startTime)
                .endTime(endTime)
                .imageUrl(imageUrl)
                .recuitLimit(recuitLimit)
                .postType(postType)
                .isAddRecruit(isAddRecruit)
                .build();
        
        return travelPostRepository.save(travelPost);
    }
    
    /**
     * 여행 게시글 수정
     * 
     * @param travelPostId 게시글 ID
     * @param userId 수정 요청자 ID
     * @param title 제목
     * @param content 내용
     * @param startTime 시작일
     * @param endTime 종료일
     * @param imageUrl 이미지 URL
     * @param recuitLimit 모집 인원
     * @param postType 게시글 타입
     * @param isAddRecruit 추가 모집 여부
     * @return 수정된 여행 게시글
     * @throws RuntimeException 권한 없음 또는 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public TravelPost updateTravelPost(Long travelPostId, Long userId, String title, String content, 
                                     LocalDate startTime, LocalDate endTime, String imageUrl, 
                                     Integer recuitLimit, TravelPost.PostType postType, Boolean isAddRecruit) {
        
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
     * 
     * @param travelPostId 게시글 ID
     * @param userId 삭제 요청자 ID
     * @return 삭제 결과 메시지
     * @throws RuntimeException 권한 없음 또는 게시글을 찾을 수 없는 경우
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
    
    /**
     * 내 일정 조회
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 내가 관련된 여행 게시글 목록
     */
    public List<TravelPost> getMySchedule(Long userId, int page, int size) {
        List<TravelPost> allSchedules = travelPostRepository.findMySchedules(userId);
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allSchedules.size());
        
        if (start >= allSchedules.size()) {
            return List.of();
        }
        
        return allSchedules.subList(start, end);
    }
    
    /**
     * 참가자 상태 변경 (승인/거절)
     * 
     * @param travelPostId 여행 게시글 ID
     * @param participantUserId 참가자 사용자 ID
     * @param newStatus 변경할 상태
     * @param currentUserId 현재 사용자 ID (권한 확인용)
     * @return 참가자 상태 변경 결과
     * @throws RuntimeException 권한 없음, 게시글 없음, 참가 신청 없음 등의 경우
     */
    @Transactional
    public ParticipationApplication updateParticipantStatus(Long travelPostId, Long participantUserId, 
                                                          ParticipationApplication.Status newStatus, Long currentUserId) {
        
        // 1. 권한 확인 - 작성자인지 확인
        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(travelPostId);
        if (travelPostOpt.isEmpty()) {
            throw new RuntimeException("Travel post not found with id: " + travelPostId);
        }
        
        TravelPost travelPost = travelPostOpt.get();
        if (!travelPost.isAuthor(currentUserId)) {
            throw new RuntimeException("Only the author can update participant status");
        }
        
        // 2. 참여 신청 조회
        Optional<ParticipationApplication> applicationOpt = participationApplicationRepository
                .findByTravelPostIdAndUserId(travelPostId, participantUserId);
        if (applicationOpt.isEmpty()) {
            throw new RuntimeException("Participation application not found");
        }
        
        ParticipationApplication application = applicationOpt.get();
        
        // 3. 상태 변경 가능 여부 확인
        if (newStatus == ParticipationApplication.Status.APPROVED && !application.canBeApproved()) {
            throw new RuntimeException("Application cannot be approved in current status: " + application.getStatus());
        }
        if (newStatus == ParticipationApplication.Status.REJECTED && !application.canBeRejected()) {
            throw new RuntimeException("Application cannot be rejected in current status: " + application.getStatus());
        }
        
        // 4. 상태 변경
        application.updateStatus(newStatus);
        participationApplicationRepository.updateStatusByTravelPostIdAndUserId(travelPostId, participantUserId, newStatus);
        
        return application;
    }
    
    /**
     * 여행 게시글 참가 신청
     * 
     * @param travelPostId 여행 게시글 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 참가 신청 응답 DTO
     */
    @Transactional
    public ParticipationApplicationResponseDto joinTravelPost(Long travelPostId, Long currentUserId) {
        // 1. 여행 게시글 존재 확인
        TravelPost travelPost = getTravelPostDetail(travelPostId);
        
        // 2. 자기 자신의 게시글에는 신청 불가
        if (travelPost.getUserId().equals(currentUserId)) {
            throw new RuntimeException("자신의 게시글에는 참가 신청할 수 없습니다.");
        }
        
        // 3. 중복 신청 방지
        if (participationApplicationRepository.existsByTravelPostIdAndUserId(travelPostId, currentUserId)) {
            throw new RuntimeException("이미 참가 신청한 게시글입니다.");
        }
        
        // 4. 모집 마감 여부 확인
        Long approvedCount = participationApplicationRepository.countByTravelPostIdAndStatus(
            travelPostId, ParticipationApplication.Status.APPROVED);
        
        if (approvedCount >= travelPost.getRecuitLimit()) {
            throw new RuntimeException("모집 인원이 마감되었습니다.");
        }
        
        // 5. 참가 신청 생성
        ParticipationApplication application = ParticipationApplication.builder()
                .travelPostId(travelPostId)
                .userId(currentUserId)
                .status(ParticipationApplication.Status.PENDING)
                .build();
        
        ParticipationApplication savedApplication = participationApplicationRepository.save(application);
        
        // 6. 사용자 정보 조회 (예외 처리 추가)
        User user;
        try {
            user = userService.getUserById(currentUserId);
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패 - userId: {}, error: {}", currentUserId, e.getMessage());
            // 기본 사용자 정보로 대체
            user = User.builder()
                    .nickname("알 수 없는 사용자")
                    .profileImgUrl("")
                    .role(UserRole.USER)
                    .oauthInfo(OauthInfo.builder().build())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        log.info("참가 신청 생성 - travelPostId: {}, userId: {}, applicationId: {}", 
                travelPostId, currentUserId, savedApplication.getId());
        
        return ParticipationApplicationResponseDto.from(savedApplication, user);
    }
    
    /**
     * 새로운 채팅방 ID 생성 (Mock 데이터)
     * 
     * @return 새로운 채팅방 ID
     */
    private Long generateNewChatRoomId() {
        // TODO: 실제 채팅방 생성 로직으로 대체
        return 1L; // Mock 데이터
    }
} 