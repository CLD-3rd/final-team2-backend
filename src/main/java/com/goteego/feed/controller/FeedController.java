package com.goteego.feed.controller;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.request.FeedCreateRequest;
import com.goteego.feed.dto.request.FeedUpdateRequest;
import com.goteego.feed.dto.response.FeedCreateResponseDto;
import com.goteego.feed.dto.response.FeedDetailResponse;
import com.goteego.feed.dto.response.FeedListResponse;
import com.goteego.feed.dto.response.FeedResponseDto;
import com.goteego.feed.service.FeedService;
import com.goteego.global.dto.SearchCondition;
import com.goteego.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 피드 목록 조회 API
     *
     * @param page      페이지 번호 (기본값: 0)
     * @param size      한 페이지에 표시할 피드 개수 (기본값: 12)
     * @param condition 검색 및 정렬 조건을 담고 있는 SearchCondition 객체
     * @return 검색 조건에 맞는 피드 목록을 포함하는 FeedListResponse 객체
     */
    @GetMapping
    public ResponseEntity<FeedListResponse> getFeeds(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @Valid SearchCondition condition) {

        FeedListResponse feeds = feedService.getFeeds(page, size, condition);
        log.info("✅ [Feed] 피드 목록 조회 성공 = 정렬: {} / 검색({})", condition.sort(), SearchCondition.getSearchTerms(condition));
        return ResponseEntity.ok(feeds);
    }

    /**
     * 피드 상세 조회 API
     * 주어진 피드 ID에 대한 상세 정보를 조회하여 반환합니다.
     *
     * @param feedId 조회할 피드의 ID
     * @return 피드 상세 정보를 담은 `FeedDetailResponse` 객체
     */
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedDetailResponse> getFeedDetail(@PathVariable("feedId") Long feedId) {

        FeedDetailResponse feedDetail = feedService.getFeedDetail(feedId);
        log.info("✅ [Feed] 피드 상세 조회 성공 - feedId: {}, title: {}", feedId, feedDetail.title());
        return ResponseEntity.ok(feedDetail);
    }

    /**
     * 피드 생성
     */
    @PostMapping
    public ResponseEntity<FeedCreateResponseDto> createFeed(
            @RequestBody FeedCreateRequest requestDto,
            @AuthenticationPrincipal User user) {

        Feed feed = feedService.createFeed(user.getId(), requestDto);

        FeedCreateResponseDto responseDto = FeedCreateResponseDto.from(feed);

        log.info("피드 생성 - feedId: {}, title: {}, userId: {}",
                feed.getId(), feed.getTitle(), user.getId());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 피드 수정
     */
    @PutMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(
            @PathVariable("feedId") Long feedId,
            @RequestBody FeedUpdateRequest requestDto,
            @AuthenticationPrincipal User user) {

        Feed feed = feedService.updateFeed(feedId, user.getId(), requestDto);

        FeedResponseDto responseDto = FeedResponseDto.from(feed);

        log.info("피드 수정 - feedId: {}, title: {}, userId: {}",
                feedId, feed.getTitle(), user.getId());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 피드 삭제
     */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<String> deleteFeed(
            @PathVariable("feedId") Long feedId,
            @AuthenticationPrincipal User user) {

        feedService.deleteFeed(feedId, user.getId());

        log.info("피드 삭제 - feedId: {}, userId: {}", feedId, user.getId());

        return ResponseEntity.ok("피드가 삭제되었습니다.");
    }
} 