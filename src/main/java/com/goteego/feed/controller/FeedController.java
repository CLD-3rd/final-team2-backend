package com.goteego.feed.controller;

import com.goteego.feed.dto.request.FeedPostRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
     * 피드 생성 API
     *
     * @param request 피드를 생성하기 위한 데이터 (제목, 내용, 위치, 배지 요청 여부, 이미지 등)
     * @param user    현재 인증된 사용자 정보 (피드를 생성하는 사용자)
     * @return 피드 생성 성공 시 HTTP 201 상태 코드 반환
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedCreateResponseDto> createFeed(
            @Valid @ModelAttribute FeedPostRequest request,
            @AuthenticationPrincipal User user) {

        Long createdFeedId = feedService.createFeed(user.getId(), request);
        log.info("✅ [Feed] 피드 생성 성공 - feedId: {}, userNickName: {}", createdFeedId, user.getNickname());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 피드 수정 API
     *
     * @param feedId  수정할 피드의 ID
     * @param request 피드 수정에 필요한 데이터 (제목, 내용, 위치, 배지 요청 여부, 이미지 등)
     * @param user    현재 인증된 사용자 정보 (피드 수정 권한을 확인하기 위해 사용)
     * @return 수정된 피드에 대한 응답 (204 No Content 반환)
     */
    @PutMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(
            @PathVariable("feedId") Long feedId,
            @Valid @ModelAttribute FeedPostRequest request,
            @AuthenticationPrincipal User user) {

        feedService.updateFeed(feedId, user.getId(), request);
        log.info("✅ [Feed] 피드 수정 성공 - feedId: {}, userNickName: {}", feedId, user.getNickname());
        return ResponseEntity.noContent().build();
    }

    /**
     * 피드 삭제 API
     *
     * @param feedId 삭제할 피드의 ID
     * @param user   삭제 요청을 보낸 사용자 정보 (권한 검증용)
     * @return 삭제 성공 응답 (HTTP 상태 코드 204 No Content)
     */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<String> deleteFeed(
            @PathVariable("feedId") Long feedId,
            @AuthenticationPrincipal User user) {

        feedService.deleteFeed(feedId, user.getId());
        log.info("✅ [Feed] 피드 삭제 성공 - feedId: {}, userNickName: {}", feedId, user.getNickname());
        return ResponseEntity.noContent().build();
    }
} 