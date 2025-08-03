package com.goteego.review.service;

import com.goteego.global.error.exception.AccessDeniedException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.review.domain.Review;
import com.goteego.review.dto.review.ReviewRequestDto;
import com.goteego.review.dto.review.ReviewTargetDto;
import com.goteego.review.repository.ReviewRepository;
import com.goteego.travelPost.domain.ParticipationApplication;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.travelPost.repository.ParticipationApplicationRepository;
import com.goteego.travelPost.repository.TravelPostRepository;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ParticipationApplicationRepository participationApplicationRepository;
    private final ReviewRepository reviewRepository;
    private final TravelPostRepository travelPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createReview(Long reviewerId, ReviewRequestDto request) {

        TravelPost post = travelPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        User reviewer = userRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신 리뷰 방지
        if (reviewerId.equals(request.getRevieweeId())) {
            throw new NotFoundException(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
        }

        //  중복 리뷰 확인
        boolean alreadyReviewed = reviewRepository.existsByPostIdAndReviewerIdAndRevieweeId(
                request.getPostId(), reviewerId, request.getRevieweeId());
        if (alreadyReviewed) {
            throw new NotFoundException(ErrorCode.REVIEW_DUPLICATE);
        }

        // 리뷰 작성자와 대상자가 같은 여행에 참여했는지 확인
        boolean bothParticipated = travelPostRepository.existsByPostIdAndUserIdsApproved(
                request.getPostId(), reviewerId, request.getRevieweeId());
        if (!bothParticipated) {
            throw new NotFoundException(ErrorCode.INVALID_REVIEW_TARGET);
        }

        // Review 저장
        Review review = Review.builder()
                .post(post)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .overallRating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // ✅ User 리뷰 점수 누적
        reviewee.addReview(request.getRating());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long reviewerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new AccessDeniedException(ErrorCode.UNAUTHORIZED_REVIEW_DELETE);
        }

        User reviewee = review.getReviewee();

        // 리뷰 삭제
        reviewRepository.delete(review);

        // ✅ 리뷰 카운트 및 점수 합계 감소
        reviewee.removeReview(review.getOverallRating());
    }

    public List<ReviewTargetDto> getReviewerTargetsByPost(Long travelPostId, Long UserId) {

        // 게시글 조회
        TravelPost post = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 여행 종료일이 오늘 이전인지 확인
        if (post.getEndTime().isAfter(LocalDate.now())) {
            throw new NotFoundException(ErrorCode.TRAVEL_NOT_FINISH);
        }

        // 해당 게시글의 승인된 참여자 목록 조회
        List<ParticipationApplication> companions = participationApplicationRepository.findApprovedByTravelPostId(travelPostId);

        // 게시글 작성자(방장)도 리뷰 대상이므로 추가
        User host = post.getUser();
        ParticipationApplication hostAsParticipant = ParticipationApplication.builder()
                .travelPost(post)
                .user(host)
                .build();

        companions.add(hostAsParticipant);

        // 내가 이미 리뷰한 대상자 필터링
        List<Review> myReviews = reviewRepository.findByReviewerIdAndPostId(UserId, travelPostId);
        Set<Long> alreadyReviewedUserIds = myReviews.stream()
                .map(r -> r.getReviewee().getId())
                .collect(Collectors.toSet());

        return companions.stream()
                .filter(pa -> !pa.getUser().getId().equals(UserId)) // 자기 자신 제외
                .filter(pa -> !alreadyReviewedUserIds.contains(pa.getUser().getId())) // 이미 리뷰한 유저들 제외
                .map(pa -> new ReviewTargetDto(
                        travelPostId,
                        pa.getTravelPost().getTitle(),
                        pa.getUser().getId(),
                        pa.getUser().getNickname(),
                        pa.getUser().getProfileImgUrl()
                ))
                .toList();
    }
}
