package com.goteego.feed.dto.request;


import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 생성/수정 요청 DTO
 * - 댓글 내용을 담는 단일 필드
 */
public record FeedCommentPostRequest(

        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content

) {
}
