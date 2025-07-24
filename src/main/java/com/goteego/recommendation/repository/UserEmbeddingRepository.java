package com.goteego.recommendation.repository;

import com.goteego.recommendation.domain.UserEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 임베딩 Repository
 * user_embeddings 테이블에 대한 데이터 접근을 담당
 * 벡터 유사도 계산을 위한 쿼리 메서드들을 제공
 */
@Repository
public interface UserEmbeddingRepository extends JpaRepository<UserEmbedding, Long> {
    
    /**
     * 특정 사용자의 임베딩 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 임베딩 정보 (Optional - 없을 수 있음)
     */
    Optional<UserEmbedding> findByUserId(Long userId);
    
    /**
     * 벡터 유사도 기반 사용자 검색
     * 현재 사용자와 유사한 사용자들을 찾을 때 사용
     * 
     * @param userEmbedding 기준이 되는 사용자의 임베딩 벡터
     * @param currentUserId 현재 사용자 ID (자기 자신 제외)
     * @param limit 반환할 결과 개수
     * @return 유사도 순으로 정렬된 사용자 ID와 유사도 점수 목록
     */
    @Query(value = """
        SELECT ue.user_id, 
               (ue.user_embedding <=> CAST(?1 AS vector)) as similarity
        FROM user_embeddings ue
        WHERE ue.user_id != ?2
        ORDER BY similarity ASC
        LIMIT ?3
        """, nativeQuery = true)
    List<Object[]> findSimilarUsers(String userEmbedding, Long currentUserId, int limit);
    
    /**
     * 특정 사용자와 모든 다른 사용자 간의 벡터 유사도 계산
     * 
     * @param currentUserEmbedding 현재 사용자의 임베딩 벡터
     * @param currentUserId 현재 사용자 ID (자기 자신 제외)
     * @return 사용자 ID와 유사도 점수 목록 (낮을수록 유사함)
     */
    @Query(value = """
        SELECT ue.user_id, 
               (ue.user_embedding <=> CAST(?1 AS vector)) as distance,
               (1.0 - (ue.user_embedding <=> CAST(?1 AS vector))) as similarity
        FROM user_embeddings ue
        WHERE ue.user_id != ?2
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<Object[]> calculateAllSimilarities(String currentUserEmbedding, Long currentUserId);
    
    /**
     * 특정 사용자와의 벡터 유사도 계산
     * 
     * @param userEmbedding1 첫 번째 사용자의 임베딩 벡터
     * @param userEmbedding2 두 번째 사용자의 임베딩 벡터
     * @return 두 사용자 간의 유사도 점수 (낮을수록 유사함)
     */
    @Query(value = "SELECT (CAST(?1 AS vector) <=> CAST(?2 AS vector)) as similarity", nativeQuery = true)
    Double calculateSimilarity(String userEmbedding1, String userEmbedding2);
    
    /**
     * 유효한 임베딩을 가진 사용자 수 조회
     * 
     * @return 유효한 임베딩을 가진 사용자 수
     */
    @Query("SELECT COUNT(ue) FROM UserEmbedding ue WHERE ue.userEmbedding IS NOT NULL AND ue.userEmbedding != ''")
    Long countValidEmbeddings();
    
    /**
     * 특정 사용자 ID 목록의 임베딩 조회
     * 
     * @param userIds 사용자 ID 목록
     * @return 해당 사용자들의 임베딩 목록
     */
    @Query("SELECT ue FROM UserEmbedding ue WHERE ue.userId IN :userIds")
    List<UserEmbedding> findByUserIdIn(@Param("userIds") List<Long> userIds);
} 