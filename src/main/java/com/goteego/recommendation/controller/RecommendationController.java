package com.goteego.recommendation.controller;

import com.goteego.recommendation.domain.UserEmbedding;
import com.goteego.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추천 시스템 컨트롤러
 * 사용자 간 유사도 계산 및 추천 관련 HTTP 요청을 처리하는 REST API 컨트롤러
 * 벡터 유사도 기반 추천 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * 두 사용자 간의 벡터 유사도 계산
     * 
     * @param currentUserId 현재 사용자 ID
     * @param targetUserId 대상 사용자 ID
     * @return 유사도 점수 (0~1 사이, 높을수록 유사함)
     */
    @GetMapping("/similarity")
    public ResponseEntity<Double> calculateUserSimilarity(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestParam("targetUserId") Long targetUserId) {
        
        Double similarity = recommendationService.calculateUserSimilarity(currentUserId, targetUserId);
        
        log.info("사용자 유사도 계산 - currentUserId: {}, targetUserId: {}, similarity: {}", 
                currentUserId, targetUserId, similarity);
        
        return ResponseEntity.ok(similarity);
    }
    
    /**
     * 현재 사용자와 유사한 사용자들 조회
     * 
     * @param currentUserId 현재 사용자 ID
     * @param limit 반환할 결과 개수 (기본값: 10)
     * @return 유사도 순으로 정렬된 사용자 ID와 유사도 점수 목록
     */
    @GetMapping("/similar-users")
    public ResponseEntity<List<Object[]>> findSimilarUsers(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        List<Object[]> similarUsers = recommendationService.findSimilarUsers(currentUserId, limit);
        
        log.info("유사한 사용자 조회 - currentUserId: {}, limit: {}, resultCount: {}", 
                currentUserId, limit, similarUsers.size());
        
        return ResponseEntity.ok(similarUsers);
    }
    
    /**
     * 사용자 임베딩 생성/업데이트
     * 
     * @param userId 사용자 ID
     * @param embedding 벡터 임베딩 데이터
     * @return 생성/업데이트된 사용자 임베딩
     */
    @PostMapping("/embeddings")
    public ResponseEntity<UserEmbedding> createOrUpdateUserEmbedding(
            @RequestParam("userId") Long userId,
            @RequestParam("embedding") String embedding) {
        
        UserEmbedding userEmbedding = recommendationService.createOrUpdateUserEmbedding(userId, embedding);
        
        log.info("사용자 임베딩 생성/업데이트 - userId: {}, embeddingLength: {}", 
                userId, embedding.length());
        
        return ResponseEntity.ok(userEmbedding);
    }
    
    /**
     * 사용자 임베딩 유효성 검증
     * 
     * @param userId 사용자 ID
     * @return 임베딩이 유효한지 여부
     */
    @GetMapping("/embeddings/{userId}/valid")
    public ResponseEntity<Boolean> isValidUserEmbedding(@PathVariable("userId") Long userId) {
        Boolean isValid = recommendationService.isValidUserEmbedding(userId);
        
        log.info("사용자 임베딩 유효성 검증 - userId: {}, isValid: {}", userId, isValid);
        
        return ResponseEntity.ok(isValid);
    }
    
    /**
     * 유효한 임베딩을 가진 사용자 수 조회
     * 
     * @return 유효한 임베딩을 가진 사용자 수
     */
    @GetMapping("/embeddings/count")
    public ResponseEntity<Long> countValidEmbeddings() {
        try {
            Long count = recommendationService.countValidEmbeddings();
            log.info("유효한 임베딩 수 조회 - count: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("임베딩 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(0L);
        }
    }
    
    /**
     * 특정 사용자들의 임베딩 조회
     * 
     * @param userIds 사용자 ID 목록 (쉼표로 구분)
     * @return 해당 사용자들의 임베딩 목록
     */
    @GetMapping("/embeddings")
    public ResponseEntity<List<UserEmbedding>> getUserEmbeddings(
            @RequestParam("userIds") String userIds) {
        
        List<Long> userIdList = List.of(userIds.split(","))
                .stream()
                .map(String::trim)
                .map(Long::valueOf)
                .toList();
        
        List<UserEmbedding> embeddings = recommendationService.getUserEmbeddings(userIdList);
        
        log.info("사용자 임베딩 목록 조회 - userIds: {}, resultCount: {}", userIds, embeddings.size());
        
        return ResponseEntity.ok(embeddings);
    }
    
    /**
     * 추천 시스템 상태 확인
     * 
     * @return 추천 시스템 상태 정보
     */
    @GetMapping("/status")
    public ResponseEntity<RecommendationStatus> getRecommendationStatus() {
        Long validEmbeddingCount = recommendationService.countValidEmbeddings();
        
        RecommendationStatus status = new RecommendationStatus();
        status.setValidEmbeddingCount(validEmbeddingCount);
        status.setSystemStatus("ACTIVE");
        status.setMessage("추천 시스템이 정상적으로 작동 중입니다.");
        
        log.info("추천 시스템 상태 조회 - validEmbeddingCount: {}", validEmbeddingCount);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 추천 시스템 상태 DTO
     */
    public static class RecommendationStatus {
        private Long validEmbeddingCount;
        private String systemStatus;
        private String message;
        
        // Getters and Setters
        public Long getValidEmbeddingCount() { return validEmbeddingCount; }
        public void setValidEmbeddingCount(Long validEmbeddingCount) { this.validEmbeddingCount = validEmbeddingCount; }
        
        public String getSystemStatus() { return systemStatus; }
        public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 