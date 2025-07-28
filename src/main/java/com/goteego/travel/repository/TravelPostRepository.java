package com.goteego.travel.repository;

import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.enumerate.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPostRepository extends JpaRepository<TravelPost, Long> {
    
    /**
     * 게시글 타입별 목록 조회 (현재 사용자 제외) - N+1 문제 해결을 위한 JOIN FETCH
     */
    @Query("SELECT tp FROM TravelPost tp JOIN FETCH tp.user WHERE tp.postType = :postType AND tp.user.id != :currentUserId ORDER BY tp.createdAt DESC")
    Page<TravelPost> findByPostTypeOrderByCreatedAtDescWithUser(@Param("postType") PostType postType,
                                                               @Param("currentUserId") Long currentUserId,
                                                               Pageable pageable);
    
    /**
     * 게시글 타입 별 개수 조회 (현재 사용자 제외)
     */
    @Query("SELECT COUNT(tp) FROM TravelPost tp WHERE tp.postType = :postType AND tp.user.id != :currentUserId")
    Long countByPostType(@Param("postType") PostType postType,
                         @Param("currentUserId") Long currentUserId);
    

    
    /**
     * 조회수 증가
     * 
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE TravelPost tp SET tp.viewCount = tp.viewCount + 1 WHERE tp.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
    
    /**
     * 참가 신청 데이터 삭제 (게시글 삭제 전)
     * 
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("DELETE FROM ParticipationApplication pa WHERE pa.travelPost.id = :postId")
    void deleteParticipationApplications(@Param("postId") Long postId);
    

    


    /**
     * 내 일정 조회 (작성자이거나 참여자인 게시글) - N+1 문제 해결을 위한 JOIN FETCH
     */
    @Query("""
        SELECT DISTINCT tp FROM TravelPost tp 
        JOIN FETCH tp.user 
        LEFT JOIN ParticipationApplication pa ON tp.id = pa.travelPost.id 
        WHERE tp.user.id = :userId OR pa.user.id = :userId 
        ORDER BY tp.startTime ASC
        """)
    List<TravelPost> findMySchedulesWithUser(@Param("userId") Long userId);
    
    /**
     * 특정 게시글의 참여자 목록 조회 (REJECTED 제외)
     */
    @Query("""
    SELECT pa
    FROM ParticipationApplication pa
    JOIN FETCH pa.user
    JOIN FETCH pa.travelPost
    WHERE pa.travelPost.id = :postId AND pa.status <> 'REJECTED'
    """)
    List<ParticipationApplication> findNonRejectedByPostId(@Param("postId") Long postId);
} 