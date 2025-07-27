package com.goteego.feed.service;

import com.goteego.feed.domain.FeedComment;
import com.goteego.feed.dto.FeedCommentResponseDto;
import com.goteego.feed.repository.FeedCommentRepository;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 피드 코멘트 서비스 클래스
 * 피드 코멘트 관련 비즈니스 로직을 처리하는 서비스 계층
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedCommentService {
    
    /**
     * 피드 코멘트 데이터 접근을 위한 리포지토리
     */
    private final FeedCommentRepository feedCommentRepository;
    
    /**
     * 피드 데이터 접근을 위한 리포지토리
     */
    private final FeedRepository feedRepository;
    
    /**
     * 사용자 데이터 접근을 위한 리포지토리
     */
    private final UserRepository userRepository;
    
    /**
     * 특정 피드의 코멘트 목록을 조회하는 메서드
     * 
     * @param feedId 조회할 피드 ID
     * @return 해당 피드의 코멘트 목록
     */
    public List<FeedCommentResponseDto> getCommentsByFeedId(Long feedId) {
        // 피드 존재 여부 확인
        if (!feedRepository.existsById(feedId)) {
            throw new IllegalArgumentException("피드를 찾을 수 없습니다.");
        }
        
        // 코멘트 목록 조회
        List<FeedComment> comments = feedCommentRepository.findByFeedIdOrderByCreatedAtAsc(feedId);
        
        // 코멘트 작성자들의 사용자 ID 수집
        List<Long> userIds = comments.stream()
                .map(FeedComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 사용자 ID를 키로 하는 닉네임 맵 생성
        Map<Long, String> userNicknames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        
        // FeedComment 엔티티를 FeedCommentResponseDto로 변환
        return comments.stream()
                .map(comment -> FeedCommentResponseDto.from(comment, userNicknames.get(comment.getUserId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 새로운 코멘트를 생성하는 메서드
     * 
     * @param feedId 코멘트가 달릴 피드 ID
     * @param userId 코멘트 작성자 ID
     * @param content 코멘트 내용
     * @return 생성된 코멘트 엔티티
     */
    @Transactional
    public FeedComment createComment(Long feedId, Long userId, String content) {
        // 피드 존재 여부 확인
        if (!feedRepository.existsById(feedId)) {
            throw new IllegalArgumentException("피드를 찾을 수 없습니다.");
        }
        
        // 코멘트 엔티티 생성
        FeedComment comment = FeedComment.builder()
                .feedId(feedId)
                .userId(userId)
                .content(content)
                .build();
        
        // 데이터베이스에 저장
        return feedCommentRepository.save(comment);
    }
    
    /**
     * 코멘트 정보를 수정하는 메서드
     * 작성자만 수정할 수 있도록 권한 검증을 수행함
     * 
     * @param commentId 수정할 코멘트 ID
     * @param userId 수정 요청한 사용자 ID
     * @param content 수정할 내용
     * @return 수정된 코멘트 엔티티
     * @throws IllegalArgumentException 코멘트를 찾을 수 없거나 작성자가 아닌 경우
     */
    @Transactional
    public FeedComment updateComment(Long commentId, Long userId, String content) {
        // 코멘트 조회
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("코멘트를 찾을 수 없습니다."));
        
        // 작성자 권한 검증
        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("코멘트 작성자만 수정할 수 있습니다.");
        }
        
        // 코멘트 내용 업데이트
        comment.update(content);
        return comment;
    }
    
    /**
     * 코멘트를 삭제하는 메서드
     * 작성자만 삭제할 수 있도록 권한 검증을 수행함
     * 
     * @param commentId 삭제할 코멘트 ID
     * @param userId 삭제 요청한 사용자 ID
     * @throws IllegalArgumentException 코멘트를 찾을 수 없거나 작성자가 아닌 경우
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 코멘트 조회
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("코멘트를 찾을 수 없습니다."));
        
        // 작성자 권한 검증
        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("코멘트 작성자만 삭제할 수 있습니다.");
        }
        
        // 코멘트 삭제
        feedCommentRepository.delete(comment);
    }
    
    /**
     * 특정 피드의 코멘트 개수를 조회하는 메서드
     * 
     * @param feedId 조회할 피드 ID
     * @return 해당 피드의 코멘트 개수
     */
    public long getCommentCountByFeedId(Long feedId) {
        return feedCommentRepository.countByFeedId(feedId);
    }
} 