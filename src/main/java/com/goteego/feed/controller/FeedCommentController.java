package com.goteego.feed.controller;

import com.goteego.feed.dto.request.FeedCommentPostRequest;
import com.goteego.feed.dto.response.FeedCommentResponse;
import com.goteego.feed.service.FeedCommentService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 피드 댓글 목록 조회 API
     *
     * @param feedId 조회할 피드의 ID
     * @param user   현재 로그인한 사용자 정보 (댓글이 작성된 유저인지 확인용)
     * @return 댓글 목록을 포함한 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<FeedCommentResponse>> getFeeds(
            @PathVariable("feedId") Long feedId,
            @AuthenticationPrincipal User user) {

        List<FeedCommentResponse> comments = feedCommentService.getComments(feedId, user);
        log.info("✅ [Feed] 피드 댓글 목록 조회 성공 - userNickName: {}", user.getNickname());
        return ResponseEntity.ok(comments);
    }

    /**
     * 피드 댓글 생성 API
     *
     * @param feedId  댓글이 달릴 피드 ID
     * @param request 댓글 생성 요청 데이터 (내용 필수)
     * @param user    현재 인증된 사용자
     * @return 생성 성공 시 HTTP 201 (Created) 응답 반환
     */
    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable("feedId") Long feedId,
            @RequestBody FeedCommentPostRequest request,
            @AuthenticationPrincipal User user) {

        Long newCommentId = feedCommentService.createComment(feedId, user.getId(), request);
        log.info("✅ [FeedComment] 피드 댓글 생성 성공 - feedId: {}, commentId: {}, userNickName: {}", feedId, newCommentId, user.getNickname());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 피드 댓글 수정 API
     *
     * @param feedId    수정할 댓글이 속한 피드 ID
     * @param commentId 수정할 댓글 ID
     * @param request   수정할 댓글 내용이 포함된 요청 객체
     * @param user      현재 로그인한 사용자
     * @return 수정 성공 시 HTTP 204 (No Content) 응답 반환
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable("feedId") Long feedId,
            @PathVariable("commentId") Long commentId,
            @RequestBody FeedCommentPostRequest request,
            @AuthenticationPrincipal User user) {

        feedCommentService.updateComment(commentId, user.getId(), request);
        log.info("✅ [FeedComment] 피드 댓글 수정 성공 - feedId: {}, commentId: {}, userNickName: {}", feedId, commentId, user.getNickname());
        return ResponseEntity.noContent().build();
    }

    /**
     * 피드 댓글 삭제 API
     *
     * @param feedId    삭제할 댓글이 속한 피드 ID
     * @param commentId 삭제할 댓글 ID
     * @param user      현재 로그인한 사용자
     * @return 삭제 성공 시 HTTP 204 (No Content) 응답 반환
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("feedId") Long feedId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal User user) {

        feedCommentService.deleteComment(commentId, user.getId());
        log.info("✅ [FeedComment] 피드 댓글 삭제 성공 - feedId: {}, commentId: {}, userNickName: {}", feedId, commentId, user.getNickname());
        return ResponseEntity.noContent().build();
    }
} 