package com.goteego.review.repository;

import com.goteego.review.domain.Review;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByPostIdAndReviewerIdAndRevieweeId(Long postId, Long reviewerId, Long revieweeId);

    List<Review> findByReviewerIdAndPostId(Long currentUserId, Long postId);

    @Query("SELECT COUNT(DISTINCT r.reviewer.id) FROM Review r WHERE r.reviewee.id = :revieweeId")
    int countDistinctReviewerByRevieweeId(@Param("revieweeId") Long revieweeId);

    @Query("SELECT COALESCE(SUM(r.overallRating), 0) FROM Review r WHERE r.reviewee.id = :revieweeId")
    int sumOverallRatingByRevieweeId(@Param("revieweeId") Long revieweeId);

    Optional<Review> findFirstByRevieweeIdOrderByIdDesc(Long revieweeId);

}