package com.goteego.feed.controller;

import com.goteego.feed.domain.FeedComment;
import com.goteego.feed.dto.request.FeedCommentPostRequest;
import com.goteego.feed.service.FeedCommentService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 피드 코멘트 컨트롤러
 * 피드 코멘트 관련 REST API 엔드포인트를 제공하는 컨트롤러
 *
 * @author GotEEgo Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds/{feedId}/comments")
@RequiredArgsConstructor
public class FeedCommentController {

    private final FeedCommentService feedCommentService;

    /**
     * 피드 댓글 생성 API
     *
     * @param feedId  댓글이 달릴 피드 ID
     * @param request 댓글 생성 요청 데이터 (내용 필수)
     * @param user    현재 인증된 사용자
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<FeedComment> createComment(
            @PathVariable("feedId") Long feedId,
            @RequestBody FeedCommentPostRequest request,
            @AuthenticationPrincipal User user) {

        Long newCommentId = feedCommentService.createComment(feedId, user.getId(), request);
        log.info("✅ [FeedComment] 피드 댓글 생성 성공 - feedId: {}, commentId: {}, userNickName: {}", feedId, newCommentId, user.getNickname());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 코멘트 수정
     *
     * @param feedId     피드 ID
     * @param commentId  코멘트 ID
     * @param requestDto 코멘트 수정 요청 데이터
     * @param user       현재 로그인한 사용자
     * @return 수정된 코멘트
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<FeedComment> updateComment(
            @PathVariable("feedId") Long feedId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequest requestDto,
            @AuthenticationPrincipal User user) {

        FeedComment comment = feedCommentService.updateComment(
                commentId,
                user.getId(),
                requestDto.getContent()
        );

        log.info("피드 코멘트 수정 - feedId: {}, commentId: {}, userId: {}",
                feedId, commentId, user.getId());

        return ResponseEntity.ok(comment);
    }

    /**
     * 코멘트 삭제
     *
     * @param feedId    피드 ID
     * @param commentId 코멘트 ID
     * @param user      현재 로그인한 사용자
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("feedId") Long feedId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal User user) {

        feedCommentService.deleteComment(commentId, user.getId());

        log.info("피드 코멘트 삭제 - feedId: {}, commentId: {}, userId: {}",
                feedId, commentId, user.getId());

        return ResponseEntity.ok("코멘트가 삭제되었습니다.");
    }

    /**
     * 코멘트 생성 요청 DTO
     */
    public static class CommentCreateRequest {
        private String content;

        // Getters and Setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 코멘트 수정 요청 DTO
     */
    public static class CommentUpdateRequest {
        private String content;

        // Getters and Setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
} 