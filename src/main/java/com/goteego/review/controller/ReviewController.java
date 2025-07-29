package com.goteego.review.controller;

import com.goteego.review.dto.review.ApiResponse;
import com.goteego.review.dto.review.ReviewRequestDto;
import com.goteego.review.dto.review.ReviewTargetDto;
import com.goteego.review.dto.review.UserReviewSummaryDto;
import com.goteego.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/review")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 특정 게시글의 리뷰 대상 조회 ***
     */
    @GetMapping("/eligible-users/{travelPostId}/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewTargetDto>>> getReviewerTargetsByPostId(
            @PathVariable("travelPostId") Long travelPostId,
            @PathVariable("userId") Long userId) {


        List<ReviewTargetDto> targets = reviewService.getReviewerTargetsByPost(travelPostId, userId);
        ApiResponse<List<ReviewTargetDto>> response = ApiResponse.of("리뷰대상자리스트 조회", targets);

        return ResponseEntity.ok(response);
    }


    /**
     * 리뷰 생성
     */
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> createReview(@RequestBody ReviewRequestDto request, @PathVariable("userId") Long userId) {
        reviewService.createReview(userId, request);
        ApiResponse<List<ReviewTargetDto>> response = ApiResponse.of("리뷰등록완료");
        return ResponseEntity.ok(response);
    }


    /**
     * 사용자 평균 별점 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserReviewSummaryDto>> getUserAverageRating(@PathVariable Long userId) {
        UserReviewSummaryDto userReviewSummaryDto = reviewService.getUserReviewSummary(userId);
        ApiResponse<UserReviewSummaryDto> response = ApiResponse.of("사용자 리뷰 조회", userReviewSummaryDto);
        return ResponseEntity.ok(response);
    }


}