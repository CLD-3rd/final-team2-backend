package com.goteego.feed.repository;

import com.goteego.feed.domain.FeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 피드 코멘트 리포지토리 인터페이스
 * FeedComment 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리
 */
@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    
    /**
     * 특정 피드의 모든 코멘트를 생성일 기준 오름차순으로 조회 (Fetch Join으로 N+1 문제 해결)
     */
    @Query("SELECT c FROM FeedComment c JOIN FETCH c.author WHERE c.feed.id = :feedId ORDER BY c.createdAt ASC")
    List<FeedComment> findByFeedIdWithAuthorOrderByCreatedAtAsc(@Param("feedId") Long feedId);
    
    /**
     * 특정 피드의 코멘트를 페이징하여 조회
     */
    @Query("SELECT c FROM FeedComment c WHERE c.feed.id = :feedId ORDER BY c.createdAt ASC")
    Page<FeedComment> findByFeedIdOrderByCreatedAtAsc(@Param("feedId") Long feedId, Pageable pageable);
    
    /**
     * 특정 피드의 코멘트 개수를 조회
     */
    @Query("SELECT COUNT(c) FROM FeedComment c WHERE c.feed.id = :feedId")
    long countByFeedId(@Param("feedId") Long feedId);
    
    /**
     * 특정 사용자가 작성한 코멘트 목록을 조회
     */
    @Query("SELECT c FROM FeedComment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC")
    List<FeedComment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
} 