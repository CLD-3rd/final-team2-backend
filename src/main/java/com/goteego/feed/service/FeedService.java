package com.goteego.feed.service;

import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.badge.repository.LandmarkBadgeRequestReposiroty;
import com.goteego.feed.domain.Feed;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.feed.domain.enumerate.FeedSortType;
import com.goteego.feed.dto.request.FeedCreateRequest;
import com.goteego.feed.dto.request.FeedUpdateRequest;
import com.goteego.feed.dto.response.FeedCommentResponseDto;
import com.goteego.feed.dto.response.FeedDetailResponseDto;
import com.goteego.feed.dto.response.FeedListResponseDto;
import com.goteego.feed.dto.response.FeedResponseDto;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     * 피드 목록을 조회하는 메서드
     */
    public FeedListResponseDto getFeeds(String title, String author, String region, String sort, int page, int size) {
        // 페이징 정보 생성
        Pageable pageable = PageRequest.of(page, size);
        
        // 정렬 타입 변환
        FeedSortType sortType = FeedSortType.fromValue(sort);
        
        // 조건에 따른 피드 조회
        Page<Feed> feedPage = getFeedsByCondition(title, author, region, sortType, pageable);
        
        // Feed 엔티티를 FeedResponseDto로 변환 (이미 Fetch Join으로 N+1 문제 해결됨)
        List<FeedResponseDto> feedDtos = feedPage.getContent().stream()
                .map(FeedResponseDto::from)
                .collect(Collectors.toList());
        
        // 페이지 정보 생성
        FeedListResponseDto.PageInfoDto pageInfo = FeedListResponseDto.PageInfoDto.builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(feedPage.getTotalPages())
                .totalElements(feedPage.getTotalElements())
                .build();
        
        // 최종 응답 DTO 생성
        return FeedListResponseDto.builder()
                .feeds(feedDtos)
                .pageInfo(pageInfo)
                .build();
    }
    
    /**
     * 검색 조건과 정렬 기준에 따른 피드 조회 메서드
     */
    private Page<Feed> getFeedsByCondition(String title, String author, String region, FeedSortType sortType, Pageable pageable) {
        switch (sortType) {
            case VIEW:
                return getFeedsByViewCount(title, author, region, pageable);
            case LIKE:
                return getFeedsByLikeCount(title, author, region, pageable);
            case RECENT:
            default:
                return getFeedsByRecent(title, author, region, pageable);
        }
    }
    
    /**
     * 최근순으로 피드를 조회하는 메서드
     */
    private Page<Feed> getFeedsByRecent(String title, String author, String region, Pageable pageable) {
        if (title != null && !title.trim().isEmpty()) {
            return feedRepository.findByTitleContainingOrderByCreatedAtDesc(title.trim(), pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            return feedRepository.findByAuthorNicknameContainingOrderByCreatedAtDesc(author.trim(), pageable);
        } else if (region != null && !region.trim().isEmpty()) {
            Location location = Location.valueOf(region.toUpperCase());
            return feedRepository.findByLocationOrderByCreatedAtDesc(location, pageable);
        } else {
            return feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }
    
    /**
     * 조회순으로 피드를 조회하는 메서드
     */
    private Page<Feed> getFeedsByViewCount(String title, String author, String region, Pageable pageable) {
        if (title != null && !title.trim().isEmpty()) {
            return feedRepository.findByTitleContainingOrderByViewCountDesc(title.trim(), pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            return feedRepository.findByAuthorNicknameContainingOrderByViewCountDesc(author.trim(), pageable);
        } else if (region != null && !region.trim().isEmpty()) {
            Location location = Location.valueOf(region.toUpperCase());
            return feedRepository.findByLocationOrderByViewCountDesc(location, pageable);
        } else {
            return feedRepository.findAllByOrderByViewCountDesc(pageable);
        }
    }
    
    /**
     * 좋아요순으로 피드를 조회하는 메서드
     */
    private Page<Feed> getFeedsByLikeCount(String title, String author, String region, Pageable pageable) {
        if (title != null && !title.trim().isEmpty()) {
            return feedRepository.findByTitleContainingOrderByLikeCountDesc(title.trim(), pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            return feedRepository.findByAuthorNicknameContainingOrderByLikeCountDesc(author.trim(), pageable);
        } else if (region != null && !region.trim().isEmpty()) {
            Location location = Location.valueOf(region.toUpperCase());
            return feedRepository.findByLocationOrderByLikeCountDesc(location, pageable);
        } else {
            return feedRepository.findAllByOrderByLikeCountDesc(pageable);
        }
    }
    
    /**
     * 새로운 피드를 생성하는 메서드
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
     * 뱃지요청 저장 매서드
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
    /**
     * 피드 정보를 수정하는 메서드
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
     * 피드를 삭제하는 메서드
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
     * 피드 상세 정보를 조회하는 메서드
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
} 