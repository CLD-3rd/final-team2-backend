package com.goteego.feed.service;

import com.goteego.feed.domain.Feed;
import com.goteego.feed.domain.FeedComment;
import com.goteego.feed.dto.request.FeedCommentPostRequest;
import com.goteego.feed.dto.response.FeedCommentResponse;
import com.goteego.feed.repository.FeedCommentRepository;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
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
    private final UserService userService;

    /**
     * 피드 댓글 목록 조회
     *
     * @param feedId 조회할 피드의 ID
     * @param user   현재 로그인한 사용자 정보 (댓글이 해당 유저의 댓글인지 확인)
     * @return 해당 피드에 달린 댓글 목록을 포함한 리스트
     */
    public List<FeedCommentResponse> getComments(Long feedId, User user) {
        // 로그인 여부 확인: 로그인한 사용자 ID를 추출
        Long currentUserId = (user != null) ? user.getId() : null;

        // 피드 존재 여부 확인: 피드가 존재하지 않으면 404 오류 발생
        if (!feedRepository.existsById(feedId))
            throw new NotFoundException(ErrorCode.FEED_NOT_FOUND);

        // 코멘트 목록 조회: 댓글 작성자와 함께 댓글을 생성 시간 순으로 조회 (N+1 문제 해결을 위한 Fetch Join 사용)
        List<FeedComment> comments = feedCommentRepository.findByFeedIdWithAuthorOrderByCreatedAtAsc(feedId);

        // FeedComment 엔티티를 FeedCommentResponse로 변환하여 반환
        return comments.stream()
                .map(comment -> FeedCommentResponse.from(comment, currentUserId))  // 로그인한 사용자와 비교하여 isMyComment 설정
                .collect(Collectors.toList());
    }

    /**
     * 피드 댓글 생성
     *
     * @param feedId  댓글이 달릴 피드 ID
     * @param userId  댓글 작성자 ID
     * @param request 댓글 생성 요청 데이터
     * @return 생성된 댓글 ID
     */
    @Transactional
    public Long createComment(Long feedId, Long userId, FeedCommentPostRequest request) {
        // 피드 존재 여부 검증 및 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 사용자 검증 및 조회
        User user = userService.getUserById(userId);

        // 댓글 엔티티 생성
        FeedComment comment = FeedComment.builder()
                .feed(feed)
                .author(user)
                .content(request.content())
                .build();

        // DB 저장 및 ID 반환
        Long commentId = feedCommentRepository.save(comment).getId();
        return commentId;
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
}