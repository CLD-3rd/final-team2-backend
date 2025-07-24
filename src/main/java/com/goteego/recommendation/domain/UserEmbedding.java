package com.goteego.recommendation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 사용자 임베딩 도메인 엔티티
 * 사용자의 선호도, 성향 등을 30차원 벡터로 표현한 도메인 객체
 * 벡터 유사도 계산을 통해 사용자 간 호환성을 측정하는 데 사용
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_embeddings")
public class UserEmbedding {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 사용자 임베딩 벡터 (30차원)
     * PostgreSQL의 vector 타입으로 저장
     * 사용자의 선호도, 성향 등을 수치화한 벡터
     * 실제로는 23차원 데이터만 사용 (나머지 7차원은 0)
     */
    @Column(name = "user_embedding", columnDefinition = "vector(30)")
    private String userEmbedding;
    
    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    @Builder
    public UserEmbedding(Long userId, String userEmbedding) {
        this.userId = userId;
        this.userEmbedding = userEmbedding;
    }
    
    /**
     * 임베딩 벡터 업데이트
     */
    public void updateEmbedding(String newEmbedding) {
        if (newEmbedding == null || newEmbedding.trim().isEmpty()) {
            throw new IllegalArgumentException("Embedding cannot be null or empty");
        }
        this.userEmbedding = newEmbedding;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * 벡터 차원 수 확인 (대략적)
     */
    public int getVectorDimension() {
        if (userEmbedding == null) return 0;
        // 벡터 문자열에서 대괄호 제거 후 쉼표로 분할
        String cleanVector = userEmbedding.replaceAll("[\\[\\]]", "");
        return cleanVector.split(",").length;
    }
    
    /**
     * 유효한 임베딩인지 확인
     */
    public boolean isValid() {
        return userEmbedding != null && !userEmbedding.trim().isEmpty();
    }
} 