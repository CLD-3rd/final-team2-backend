package com.goteego.recommendation.service;

import com.goteego.recommendation.domain.UserEmbedding;
import com.goteego.recommendation.repository.UserEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 추천 시스템 서비스
 * 사용자 간 유사도 계산 및 추천 로직을 담당하는 서비스 클래스
 * 벡터 유사도 기반 추천 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {
    
    private final UserEmbeddingRepository userEmbeddingRepository;
    
    /**
     * 두 사용자 간의 벡터 유사도 계산
     * 
     * @param currentUserId 현재 사용자 ID
     * @param targetUserId 대상 사용자 ID
     * @return 유사도 점수 (0~1 사이, 높을수록 유사함)
     */
    public Double calculateUserSimilarity(Long currentUserId, Long targetUserId) {
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
     * 현재 사용자와 유사한 사용자들 조회
     * 
     * @param currentUserId 현재 사용자 ID
     * @param limit 반환할 결과 개수
     * @return 유사도 순으로 정렬된 사용자 ID와 유사도 점수 목록
     */
    public List<Object[]> findSimilarUsers(Long currentUserId, int limit) {
        Optional<UserEmbedding> currentUserEmbedding = userEmbeddingRepository.findByUserId(currentUserId);
        
        if (currentUserEmbedding.isEmpty()) {
            log.warn("현재 사용자 임베딩이 없어서 빈 목록 반환");
            return List.of();
        }
        
        return userEmbeddingRepository.findSimilarUsers(
            currentUserEmbedding.get().getUserEmbedding(),
            currentUserId,
            limit
        );
    }
    
    /**
     * 사용자 임베딩 생성/업데이트
     * 
     * @param userId 사용자 ID
     * @param embedding 벡터 임베딩 데이터
     * @return 생성/업데이트된 사용자 임베딩
     */
    @Transactional
    public UserEmbedding createOrUpdateUserEmbedding(Long userId, String embedding) {
        Optional<UserEmbedding> existingEmbedding = userEmbeddingRepository.findByUserId(userId);
        
        if (existingEmbedding.isPresent()) {
            UserEmbedding userEmbedding = existingEmbedding.get();
            userEmbedding.updateEmbedding(embedding);
            return userEmbeddingRepository.save(userEmbedding);
        } else {
            UserEmbedding newEmbedding = UserEmbedding.builder()
                    .userId(userId)
                    .userEmbedding(embedding)
                    .build();
            return userEmbeddingRepository.save(newEmbedding);
        }
    }
    
    /**
     * 사용자 임베딩 유효성 검증
     * 
     * @param userId 사용자 ID
     * @return 임베딩이 유효한지 여부
     */
    public boolean isValidUserEmbedding(Long userId) {
        Optional<UserEmbedding> userEmbedding = userEmbeddingRepository.findByUserId(userId);
        return userEmbedding.isPresent() && userEmbedding.get().isValid();
    }
    
    /**
     * 유효한 임베딩을 가진 사용자 수 조회
     * 
     * @return 유효한 임베딩을 가진 사용자 수
     */
    public Long countValidEmbeddings() {
        return userEmbeddingRepository.countValidEmbeddings();
    }
    
    /**
     * 특정 사용자들의 임베딩 조회
     * 
     * @param userIds 사용자 ID 목록
     * @return 해당 사용자들의 임베딩 목록
     */
    public List<UserEmbedding> getUserEmbeddings(List<Long> userIds) {
        return userEmbeddingRepository.findByUserIdIn(userIds);
    }
} 