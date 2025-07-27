package com.goteego.feed.controller;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.FeedDetailResponseDto;
import com.goteego.feed.dto.FeedListResponseDto;
import com.goteego.feed.service.FeedService;
import com.goteego.user.domain.User;
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
     * 피드 목록 조회
     * 
     * @param author 글쓴이 검색 (선택사항)
     * @param location 위치 검색 (선택사항)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 피드 목록과 페이징 정보
     */
    @GetMapping
    public ResponseEntity<FeedListResponseDto> getFeeds(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        
        FeedListResponseDto feeds = feedService.getFeeds(author, location, page, size);
        
        log.info("피드 목록 조회 - author: {}, location: {}, page: {}, size: {}, totalElements: {}", 
                author, location, page, size, feeds.getPageInfo().getTotalElements());
        
        return ResponseEntity.ok(feeds);
    }
    
    /**
     * 피드 상세 조회
     * 
     * @param feedId 피드 ID
     * @return 피드 상세 정보와 코멘트 목록
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
     * 
     * @param requestDto 피드 생성 요청 데이터
     * @param user 현재 로그인한 사용자
     * @return 생성된 피드
     */
    @PostMapping
    public ResponseEntity<Feed> createFeed(
            @RequestBody FeedCreateRequest requestDto,
            @AuthenticationPrincipal User user) {
        
        Feed feed = feedService.createFeed(
            user.getId(),
            requestDto.getTitle(),
            requestDto.getContent(),
            requestDto.getImageUrl(),
            requestDto.getLocation()
        );
        
        log.info("피드 생성 - feedId: {}, title: {}, userId: {}", 
                feed.getId(), feed.getTitle(), user.getId());
        
        return ResponseEntity.ok(feed);
    }
    
    /**
     * 피드 수정
     * 
     * @param feedId 피드 ID
     * @param requestDto 피드 수정 요청 데이터
     * @param user 현재 로그인한 사용자
     * @return 수정된 피드
     */
    @PutMapping("/{feedId}")
    public ResponseEntity<Feed> updateFeed(
            @PathVariable("feedId") Long feedId,
            @RequestBody FeedUpdateRequest requestDto,
            @AuthenticationPrincipal User user) {
        
        Feed feed = feedService.updateFeed(
            feedId,
            user.getId(),
            requestDto.getTitle(),
            requestDto.getContent(),
            requestDto.getImageUrl(),
            requestDto.getLocation()
        );
        
        log.info("피드 수정 - feedId: {}, title: {}, userId: {}", 
                feedId, feed.getTitle(), user.getId());
        
        return ResponseEntity.ok(feed);
    }
    
    /**
     * 피드 삭제
     * 
     * @param feedId 피드 ID
     * @param user 현재 로그인한 사용자
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<String> deleteFeed(
            @PathVariable("feedId") Long feedId,
            @AuthenticationPrincipal User user) {
        
        feedService.deleteFeed(feedId, user.getId());
        
        log.info("피드 삭제 - feedId: {}, userId: {}", feedId, user.getId());
        
        return ResponseEntity.ok("피드가 삭제되었습니다.");
    }
    
    /**
     * 피드 생성 요청 DTO
     */
    public static class FeedCreateRequest {
        private String title;
        private String content;
        private String imageUrl;
        private String location;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    
    /**
     * 피드 수정 요청 DTO
     */
    public static class FeedUpdateRequest {
        private String title;
        private String content;
        private String imageUrl;
        private String location;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
} 