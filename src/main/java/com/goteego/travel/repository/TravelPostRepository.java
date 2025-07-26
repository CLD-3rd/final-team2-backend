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
     * 게시글 타입별 목록 조회 (현재 사용자 제외)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.postType = :postType And tp.user.id != :currentUserId ORDER BY tp.createdAt DESC")
    Page<TravelPost> findByPostTypeOrderByCreatedAtDesc(@Param("postType") PostType postType,
                                                        @Param("currentUserId") Long currentUserId,
                                                        Pageable pageable);
    
    /**
     * 게시글 타입 별 개수 조회 (현재 사용자 제외)
     */
    @Query("SELECT COUNT(tp) FROM TravelPost tp WHERE tp.postType = :postType AND tp.user.id != :currentUserId")
    Long countByPostType(@Param("postType") PostType postType,
                         @Param("currentUserId") Long currentUserId);
    
    /**
     * 특정 게시글 조회
     * 
     * @param postId 게시글 ID
     * @return 여행 게시글 정보 (Optional)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.id = :postId")
    Optional<TravelPost> findById(@Param("postId") Long postId);
    
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
    @Query(value = "DELETE FROM participation_application WHERE travel_post_id = :postId", nativeQuery = true)
    void deleteParticipationApplications(@Param("postId") Long postId);
    
    /**
     * 사용자 리뷰 데이터 삭제 (게시글 삭제 전)
     * 
     * @param postId 게시글 ID
     */
    @Modifying
    @Query(value = "DELETE FROM user_review WHERE post_id = :postId", nativeQuery = true)
    void deleteUserReviews(@Param("postId") Long postId);
    
    /**
     * 내 일정 조회 (작성자이거나 참여자인 게시글)
     * 
     * @param userId 사용자 ID
     * @return 내가 관련된 여행 게시글 목록
     */
    @Query(value = """
        SELECT DISTINCT tp.* FROM travel_posts tp
        LEFT JOIN participation_application pa ON tp.travel_post_id = pa.travel_post_id
        WHERE tp.user_id = :userId OR pa.user_id = :userId
        ORDER BY tp.start_time ASC
        """, nativeQuery = true)
    List<TravelPost> findMySchedules(@Param("userId") Long userId);
    
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