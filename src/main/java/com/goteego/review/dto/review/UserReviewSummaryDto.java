package com.goteego.review.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewSummaryDto {
    private Long revieweeId;
    private int totalReviews;
    private int totalRatingScore;
    private double averageRating;

}