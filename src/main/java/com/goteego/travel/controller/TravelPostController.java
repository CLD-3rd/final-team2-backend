package com.goteego.travel.controller;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.dto.TravelPostResponseDto;
import com.goteego.travel.service.TravelPostService;
import com.goteego.travel.service.ScheduleService;
import com.goteego.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.goteego.travel.dto.PageResponseDto;
import com.goteego.travel.dto.ParticipationApplicationResponseDto;
import com.goteego.global.jwt.JwtTokenProvider;

/**
 * 여행 게시글 컨트롤러
 * 여행 게시글 관련 HTTP 요청을 처리하는 REST API 컨트롤러
 * 게시글 CRUD, 목록 조회 등의 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-posts")
@RequiredArgsConstructor
public class TravelPostController {
    
    private final TravelPostService travelPostService;
    private final ScheduleService scheduleService;
    private final RecommendationService recommendationService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 여행 게시글 목록 조회 (벡터 유사도 기반)
     * 
     * @param postType 게시글 타입 (BEFORE/NOW)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param authorization 인증 헤더 (Bearer 토큰)
     * @return 페이징된 여행 게시글 목록 (유사도 점수 포함)
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<TravelPostResponseDto>> getTravelPosts(
            @RequestParam(value = "postType", defaultValue = "BEFORE") String postType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        TravelPost.PostType type = TravelPost.PostType.valueOf(postType.toUpperCase());
        
        PageResponseDto<TravelPostResponseDto> travelPosts = travelPostService.getTravelPosts(type, page, size, currentUserId);
        
        log.info("여행 게시글 목록 조회 - postType: {}, page: {}, size: {}, totalElements: {}", 
                postType, page, size, travelPosts.getTotalElements());
        
        return ResponseEntity.ok(travelPosts);
    }
    
    /**
     * 여행 게시글 상세 조회
     * 
     * @param travelPostId 게시글 ID
     * @return 여행 게시글 상세 정보
     */
    @GetMapping("/{travelPostId}")
    public ResponseEntity<TravelPost> getTravelPostDetail(@PathVariable("travelPostId") Long travelPostId) {
        TravelPost travelPost = travelPostService.getTravelPostDetail(travelPostId);
        
        log.info("여행 게시글 상세 조회 - travelPostId: {}, title: {}", travelPostId, travelPost.getTitle());
        
        return ResponseEntity.ok(travelPost);
    }
    
    /**
     * 여행 게시글 생성
     * 
     * @param requestDto 게시글 생성 요청 데이터
     * @param authorization 인증 헤더
     * @return 생성된 여행 게시글
     */
    @PostMapping("/requests")
    public ResponseEntity<TravelPost> createTravelPost(
            @RequestBody TravelPostCreateRequest requestDto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        
        TravelPost travelPost = travelPostService.createTravelPost(
            currentUserId,
            requestDto.getTitle(),
            requestDto.getContent(),
            requestDto.getStartTime(),
            requestDto.getEndTime(),
            requestDto.getImageUrl(),
            requestDto.getRecuitLimit(),
            TravelPost.PostType.valueOf(requestDto.getPostType().toUpperCase()),
            requestDto.getIsAddRecruit()
        );
        
        log.info("여행 게시글 생성 - travelPostId: {}, title: {}, userId: {}", 
                travelPost.getId(), travelPost.getTitle(), currentUserId);
        
        return ResponseEntity.ok(travelPost);
    }
    
    /**
     * 여행 게시글 수정
     * 
     * @param travelPostId 게시글 ID
     * @param requestDto 게시글 수정 요청 데이터
     * @param authorization 인증 헤더
     * @return 수정된 여행 게시글
     */
    @PutMapping("/{travelPostId}")
    public ResponseEntity<TravelPost> updateTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @RequestBody TravelPostUpdateRequest requestDto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        
        TravelPost travelPost = travelPostService.updateTravelPost(
            travelPostId,
            currentUserId,
            requestDto.getTitle(),
            requestDto.getContent(),
            requestDto.getStartTime(),
            requestDto.getEndTime(),
            requestDto.getImageUrl(),
            requestDto.getRecuitLimit(),
            TravelPost.PostType.valueOf(requestDto.getPostType().toUpperCase()),
            requestDto.getIsAddRecruit()
        );
        
        log.info("여행 게시글 수정 - travelPostId: {}, title: {}, userId: {}", 
                travelPostId, travelPost.getTitle(), currentUserId);
        
        return ResponseEntity.ok(travelPost);
    }
    
    /**
     * 여행 게시글 삭제
     * 
     * @param travelPostId 게시글 ID
     * @param authorization 인증 헤더
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{travelPostId}")
    public ResponseEntity<String> deleteTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        
        String result = travelPostService.deleteTravelPost(travelPostId, currentUserId);
        
        log.info("여행 게시글 삭제 - travelPostId: {}, userId: {}", travelPostId, currentUserId);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 여행 게시글 참가 신청
     * 
     * @param travelPostId 여행 게시글 ID
     * @param authorization 인증 헤더 (Bearer 토큰)
     * @return 참가 신청 응답
     */
    @PostMapping("/requests/{travelPostId}")
    public ResponseEntity<ParticipationApplicationResponseDto> joinTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        
        ParticipationApplicationResponseDto response = travelPostService.joinTravelPost(travelPostId, currentUserId);
        
        log.info("참가 신청 - travelPostId: {}, userId: {}, applicationId: {}", 
                travelPostId, currentUserId, response.getApplicationId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     * 
     * @param authorization 인증 헤더
     * @return 사용자 ID
     */
    private Long extractUserIdFromToken(String authorization) {
        log.info("=== extractUserIdFromToken 시작 ===");
        log.info("authorization: {}", authorization);
        
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Authorization 헤더가 없습니다.");
            throw new IllegalArgumentException("Authorization 헤더가 필요합니다.");
        }
        
        if (authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            log.info("JWT 토큰: {}", token);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserId(token);
                    log.info("토큰에서 추출한 사용자 ID: {}", userId);
                    return userId;
                } else {
                    log.warn("유효하지 않은 JWT 토큰입니다.");
                    throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
                }
            } catch (Exception e) {
                log.error("토큰 파싱 중 오류 발생: {}", e.getMessage());
                throw new IllegalArgumentException("토큰 파싱 중 오류가 발생했습니다.");
            }
        }
        
        log.warn("올바르지 않은 Authorization 헤더 형식입니다.");
        throw new IllegalArgumentException("올바르지 않은 Authorization 헤더 형식입니다.");
    }
    
    /**
     * 여행 게시글 생성 요청 DTO
     */
    public static class TravelPostCreateRequest {
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private String postType;
        private Boolean isAddRecruit;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public LocalDate getStartTime() { return startTime; }
        public void setStartTime(LocalDate startTime) { this.startTime = startTime; }
        
        public LocalDate getEndTime() { return endTime; }
        public void setEndTime(LocalDate endTime) { this.endTime = endTime; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Integer getRecuitLimit() { return recuitLimit; }
        public void setRecuitLimit(Integer recuitLimit) { this.recuitLimit = recuitLimit; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public Boolean getIsAddRecruit() { return isAddRecruit; }
        public void setIsAddRecruit(Boolean isAddRecruit) { this.isAddRecruit = isAddRecruit; }
    }
    
    /**
     * 여행 게시글 수정 요청 DTO
     */
    public static class TravelPostUpdateRequest {
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private String postType;
        private Boolean isAddRecruit;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public LocalDate getStartTime() { return startTime; }
        public void setStartTime(LocalDate startTime) { this.startTime = startTime; }
        
        public LocalDate getEndTime() { return endTime; }
        public void setEndTime(LocalDate endTime) { this.endTime = endTime; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Integer getRecuitLimit() { return recuitLimit; }
        public void setRecuitLimit(Integer recuitLimit) { this.recuitLimit = recuitLimit; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public Boolean getIsAddRecruit() { return isAddRecruit; }
        public void setIsAddRecruit(Boolean isAddRecruit) { this.isAddRecruit = isAddRecruit; }
    }
} 