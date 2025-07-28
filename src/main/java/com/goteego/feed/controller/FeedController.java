package com.goteego.feed.controller;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.request.FeedCreateRequest;
import com.goteego.feed.dto.request.FeedUpdateRequest;
import com.goteego.feed.dto.response.FeedCreateResponseDto;
import com.goteego.feed.dto.response.FeedDetailResponseDto;
import com.goteego.feed.dto.response.FeedListResponseDto;
import com.goteego.feed.dto.response.FeedResponseDto;
import com.goteego.feed.service.FeedService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    
    private final FeedService feedService;
    
    /**
     * 피드 목록 조회
     */
    @GetMapping
    public ResponseEntity<FeedListResponseDto> getFeeds(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        
        FeedListResponseDto feeds = feedService.getFeeds(title, author, region, sort, page, size);
        
        log.info("피드 목록 조회 - title: {}, author: {}, region: {}, sort: {}, page: {}, size: {}, totalElements: {}", 
                title, author, region, sort, page, size, feeds.getPageInfo().getTotalElements());
        
        return ResponseEntity.ok(feeds);
    }
    
    /**
     * 피드 상세 조회
     */
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedDetailResponseDto> getFeedDetail(@PathVariable("feedId") Long feedId) {
        FeedDetailResponseDto feedDetail = feedService.getFeedDetail(feedId);
        
        log.info("피드 상세 조회 - feedId: {}, title: {}, commentCount: {}", 
                feedId, feedDetail.getTitle(), feedDetail.getComments().size());
        
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