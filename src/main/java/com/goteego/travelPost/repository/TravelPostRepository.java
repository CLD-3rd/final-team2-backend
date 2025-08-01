package com.goteego.travelPost.repository;

import com.goteego.global.domain.enumerate.Location;
import com.goteego.travelPost.domain.ParticipationApplication;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.travelPost.domain.enumerate.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPostRepository extends JpaRepository<TravelPost, Long> {

    /**
     * 게시글 검색 조건에 따라 TravelPost 목록을 조회하는 메서드
     * - PostType, 제목, 작성자, 지역 조건 필터링
     * - N+1 문제 해결을 위해 User 엔티티를 즉시 로딩 (EntityGraph 사용)
     * - 페이징 및 정렬 지원 (Pageable)
     *
     * @param postType 게시글 타입 (BEFORE / NOW)
     * @param title    검색할 게시글 제목 (null 허용)
     * @param author   검색할 작성자 닉네임 (null 허용)
     * @param location 검색할 지역 (null 허용)
     * @param pageable 페이징 및 정렬 정보
     * @return 조건에 맞는 게시글 Page 객체
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("""
                SELECT tp FROM TravelPost tp
                WHERE tp.postType = :postType
                AND (:title IS NULL OR tp.title LIKE %:title%)
                AND (:author IS NULL OR tp.user.nickname LIKE %:author%)
                AND (:location IS NULL OR tp.location = :location)
            """)
    Page<TravelPost> getTravelPostsWithCondition(
            @Param("postType") PostType postType,
            @Param("title") String title,
            @Param("author") String author,
            @Param("location") Location location,
            Pageable pageable
    );

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

    /**
     * 리뷰 작성자와 대상자가 같은 여행에 참여했는지 확인 (둘다 참여 2, 혼자참여 1, 아무도 참여안함 0)
     *
     * @param postId
     * @param reviewerId
     * @param revieweeId
     * @return
     */
    @Query("""
            SELECT (COUNT(DISTINCT u.id) = 2)
            FROM User u
            WHERE u.id IN (:reviewerId, :revieweeId)
            AND (
                u.id = (SELECT tp.user.id FROM TravelPost tp WHERE tp.id = :postId)
                OR u.id IN (
                    SELECT pa.user.id FROM ParticipationApplication pa
                    WHERE pa.travelPost.id = :postId AND pa.status = 'APPROVED'
                )
            )
            """)
    Boolean existsByPostIdAndUserIdsApproved(@Param("postId") Long postId,
                                             @Param("reviewerId") Long reviewerId,
                                             @Param("revieweeId") Long revieweeId);
}