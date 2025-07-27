package com.goteego.feed.service;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.FeedCommentResponseDto;
import com.goteego.feed.dto.FeedDetailResponseDto;
import com.goteego.feed.dto.FeedListResponseDto;
import com.goteego.feed.dto.FeedResponseDto;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 피드 서비스 클래스
 * 피드 관련 비즈니스 로직을 처리하는 서비스 계층
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {
    
    /**
     * 피드 데이터 접근을 위한 리포지토리
     */
    private final FeedRepository feedRepository;
    
    /**
     * 사용자 데이터 접근을 위한 리포지토리
     */
    private final UserRepository userRepository;
    
    /**
     * 피드 코멘트 서비스
     */
    private final FeedCommentService feedCommentService;
    
    /**
     * 피드 목록을 조회하는 메서드
     * 작성자나 위치로 필터링이 가능하며, 페이징을 지원함
     * 
     * @param author 검색할 작성자 닉네임 (선택사항)
     * @param location 검색할 위치 정보 (선택사항)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이징된 피드 목록과 페이지 정보
     */
    public FeedListResponseDto getFeeds(String author, String location, int page, int size) {
        // 페이징 정보 생성
        Pageable pageable = PageRequest.of(page, size);
        
        // 조건에 따른 피드 조회
        Page<Feed> feedPage = getFeedsByLatest(author, location, pageable);
        
        // 피드 작성자들의 사용자 ID 수집
        List<Long> userIds = feedPage.getContent().stream()
                .map(Feed::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 사용자 ID를 키로 하는 닉네임 맵 생성
        Map<Long, String> userNicknames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        
        // Feed 엔티티를 FeedResponseDto로 변환
        List<FeedResponseDto> feedDtos = feedPage.getContent().stream()
                .map(feed -> FeedResponseDto.from(feed, userNicknames.get(feed.getUserId())))
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
     * 검색 조건에 따른 피드 조회 메서드
     * 
     * @param author 작성자 검색 조건
     * @param location 위치 검색 조건
     * @param pageable 페이징 정보
     * @return 조건에 맞는 페이징된 피드 목록
     */
    private Page<Feed> getFeedsByLatest(String author, String location, Pageable pageable) {
        if (author != null && !author.trim().isEmpty()) {
            // 작성자로 검색
            return feedRepository.findByAuthorNicknameContainingOrderByCreatedAtDesc(author.trim(), pageable);
        } else if (location != null && !location.trim().isEmpty()) {
            // 위치로 검색
            return feedRepository.findByLocationContainingOrderByCreatedAtDesc(location.trim(), pageable);
        } else {
            // 전체 피드 조회
            return feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }
    

    
    /**
     * 새로운 피드를 생성하는 메서드
     * 
     * @param userId 피드 작성자 ID
     * @param title 피드 제목
     * @param content 피드 내용
     * @param imageUrl 이미지 URL
     * @param location 위치 정보
     * @return 생성된 피드 엔티티
     */
    @Transactional
    public Feed createFeed(Long userId, String title, String content, String imageUrl, String location) {
        // 피드 엔티티 생성
        Feed feed = Feed.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .location(location)
                .build();
        
        // 데이터베이스에 저장
        return feedRepository.save(feed);
    }
    
    /**
     * 피드 정보를 수정하는 메서드
     * 작성자만 수정할 수 있도록 권한 검증을 수행함
     * 
     * @param feedId 수정할 피드 ID
     * @param userId 수정 요청한 사용자 ID
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param imageUrl 수정할 이미지 URL
     * @param location 수정할 위치 정보
     * @return 수정된 피드 엔티티
     * @throws IllegalArgumentException 피드를 찾을 수 없거나 작성자가 아닌 경우
     */
    @Transactional
    public Feed updateFeed(Long feedId, Long userId, String title, String content, String imageUrl, String location) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));
        
        // 작성자 권한 검증
        if (!feed.isAuthor(userId)) {
            throw new IllegalArgumentException("피드 작성자만 수정할 수 있습니다.");
        }
        
        // 피드 정보 업데이트
        feed.update(title, content, imageUrl, location);
        return feed;
    }
    
    /**
     * 피드를 삭제하는 메서드
     * 작성자만 삭제할 수 있도록 권한 검증을 수행함
     * 
     * @param feedId 삭제할 피드 ID
     * @param userId 삭제 요청한 사용자 ID
     * @throws IllegalArgumentException 피드를 찾을 수 없거나 작성자가 아닌 경우
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
     * 조회 시 조회수가 자동으로 증가되며, 코멘트 목록도 함께 반환됨
     * 
     * @param feedId 조회할 피드 ID
     * @return 피드 상세 정보와 코멘트 목록
     * @throws IllegalArgumentException 피드를 찾을 수 없는 경우
     */
    @Transactional
    public FeedDetailResponseDto getFeedDetail(Long feedId) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));
        
        // 조회수 증가
        feed.incrementViewCount();
        
        // 작성자 닉네임 조회
        String authorNickname = userRepository.findById(feed.getUserId())
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");
        
        // 코멘트 목록 조회
        List<FeedCommentResponseDto> comments = feedCommentService.getCommentsByFeedId(feedId);
        
        // FeedDetailResponseDto로 변환하여 반환
        return FeedDetailResponseDto.from(feed, authorNickname, comments);
    }
} 