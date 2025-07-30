package com.goteego.feed.service;

import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.badge.repository.LandmarkBadgeRequestReposiroty;
import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.request.FeedCreateRequest;
import com.goteego.feed.dto.request.FeedUpdateRequest;
import com.goteego.feed.dto.response.FeedCommentResponseDto;
import com.goteego.feed.dto.response.FeedDetailResponseDto;
import com.goteego.feed.dto.response.FeedListResponse;
import com.goteego.feed.dto.response.FeedResponse;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.dto.PageInfo;
import com.goteego.global.dto.SearchCondition;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 피드 서비스 클래스
 * 피드 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedCommentService feedCommentService;
    private final LandmarkBadgeRequestReposiroty landmarkBadgeRequestReposiroty;

    /**
     * 피드 목록 조회
     *
     * @param page      페이지 번호 (0부터 시작)
     * @param size      한 페이지에 표시할 피드 개수
     * @param condition 검색 및 정렬 조건을 담고 있는 SearchCondition 객체
     * @return FeedListResponse 피드 목록과 페이지 정보가 포함된 응답 객체
     */
    @Transactional(readOnly = true)
    public FeedListResponse getFeeds(int page, int size, SearchCondition condition) {
        // ✅ 정렬 조건 설정
        Pageable pageable = PageRequest.of(page, size, getSortOption(condition.sort()));

        // ✅ location 검증 및 변환
        Location locationEnum = null;
        if (condition.location() != null && !condition.location().isBlank()) {
            try {
                locationEnum = Location.fromDisplayName(condition.location());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_LOCATION);
            }
        }

        // ✅ 조건에 맞는 피드 조회
        Page<Feed> feedPage = feedRepository.getFeedsWithCondition(
                condition.title(),
                condition.author(),
                locationEnum,
                pageable);
        PageInfo pageInfo = PageInfo.from(feedPage);

        // ✅ 조회된 피드를 FeedResponse DTO로 변환
        List<FeedResponse> feedResponseList = feedPage.getContent().stream()
                .map(FeedResponse::from).toList();

        // ✅ 최종 응답 DTO 생성 및 반환
        return FeedListResponse.builder()
                .feeds(feedResponseList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 피드 상세 정보 조회
     */
    @Transactional
    public FeedDetailResponseDto getFeedDetail(Long feedId) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 조회수 증가
        feed.incrementViewCount();

        // 코멘트 목록 조회
        List<FeedCommentResponseDto> comments = feedCommentService.getCommentsByFeedId(feedId);

        // FeedDetailResponseDto로 변환하여 반환
        return FeedDetailResponseDto.from(feed, comments);
    }

    /**
     * 피드 생성
     */
    @Transactional
    public Feed createFeed(Long userId, FeedCreateRequest requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 피드 엔티티 생성
        Feed feed = Feed.builder()
                .author(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageUrl(requestDto.getImageUrl())
                .location(requestDto.getLocation())
                .badgeRequest(requestDto.getBadgeRequest())
                .build();

        // 데이터베이스에 저장
        Feed savedFeed = feedRepository.save(feed);
        // 뱃지 요청
        this.requestBadgeByFeed(savedFeed);
        return savedFeed;
    }

    /**
     * 피드 수정
     */
    @Transactional
    public Feed updateFeed(Long feedId, Long userId, FeedUpdateRequest requestDto) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 작성자 권한 검증
        if (!feed.isAuthor(userId)) {
            throw new IllegalArgumentException("피드 작성자만 수정할 수 있습니다.");
        }

        // 피드 정보 수정
        feed.update(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getImageUrl(),
                requestDto.getLocation(),
                requestDto.getBadgeRequest()
        );

        return feed;
    }

    /**
     * 피드 삭제
     */
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 작성자 권한 검증
        if (!feed.isAuthor(userId)) {
            throw new IllegalArgumentException("피드 작성자만 삭제할 수 있습니다.");
        }

        // 피드 삭제
        feedRepository.delete(feed);
    }

    /**
     * 뱃지요청 저장 매서드
     *
     * @param feed
     */
    @Transactional
    public void requestBadgeByFeed(Feed feed) {
        //뱃지요청
        if (Boolean.TRUE.equals(feed.getBadgeRequest())) {
            LandmarkBadgeRequest request = LandmarkBadgeRequest.builder()
                    .feed(feed)
                    .status(BadgeStatus.PENDING)
                    .build();
            landmarkBadgeRequestReposiroty.save(request);
        }
    }

    // =====================================================내부 로직======================================================= //

    /**
     * 게시글 정렬 옵션 처리
     *
     * @param sort 정렬 기준 (recent / view), 기본값: view
     * @return Sort 객체
     */
    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "viewCount");
        };
    }
}