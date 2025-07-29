package com.goteego.feed.service;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.domain.FeedComment;
import com.goteego.feed.dto.response.FeedCommentResponseDto;
import com.goteego.feed.repository.FeedCommentRepository;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 피드 코멘트 서비스 클래스
 * 피드 코멘트 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedCommentService {
    
    private final FeedCommentRepository feedCommentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    
    /**
     * 특정 피드의 코멘트 목록을 조회하는 메서드
     */
    public List<FeedCommentResponseDto> getCommentsByFeedId(Long feedId) {
        // 피드 존재 여부 확인
        if (!feedRepository.existsById(feedId)) {
            throw new IllegalArgumentException("피드를 찾을 수 없습니다.");
        }
        
        // 코멘트 목록 조회 (Fetch Join으로 N+1 문제 해결)
        List<FeedComment> comments = feedCommentRepository.findByFeedIdWithAuthorOrderByCreatedAtAsc(feedId);
        
        // FeedComment 엔티티를 FeedCommentResponseDto로 변환
        return comments.stream()
                .map(FeedCommentResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 새로운 코멘트를 생성하는 메서드
     */
    @Transactional
    public FeedComment createComment(Long feedId, Long userId, String content) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 코멘트 엔티티 생성
        FeedComment comment = FeedComment.builder()
                .feed(feed)
                .author(user)
                .content(content)
                .build();
        
        // 데이터베이스에 저장
        return feedCommentRepository.save(comment);
    }
    
    /**
     * 코멘트 정보를 수정하는 메서드
     */
    @Transactional
    public FeedComment updateComment(Long commentId, Long userId, String content) {
        // 코멘트 조회
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        // 작성자 권한 검증
        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }
        
        // 코멘트 내용 수정
        comment.update(content);
        
        return comment;
    }
    
    /**
     * 코멘트를 삭제하는 메서드
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 코멘트 조회
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        // 작성자 권한 검증
        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        
        // 코멘트 삭제
        feedCommentRepository.delete(comment);
    }
    
    /**
     * 특정 피드의 코멘트 개수를 조회하는 메서드
     */
    public long getCommentCountByFeedId(Long feedId) {
        return feedCommentRepository.countByFeedId(feedId);
    }
} 