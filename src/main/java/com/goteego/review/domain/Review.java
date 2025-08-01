package com.goteego.review.domain;

import com.goteego.global.domain.BaseEntity;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_reviewer_reviewee", columnNames = {"post_id", "reviewer_id", "reviewee_id"})
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private TravelPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", updatable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", updatable = false)
    private User reviewee;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;  // 1~5 점수

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;


    // 누적 합산 필드
    @Column(name = "total_reviews")
    private Integer totalReviews;  // 해당 사용자 받은 리뷰 수 (누적)

    @Column(name = "total_rating_score")
    private Integer totalRatingScore;  // 받은 별점 누적 합산

}