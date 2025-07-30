package com.goteego.feed.dto.response;

import com.goteego.feed.domain.FeedComment;
import com.goteego.global.dto.Author;
import lombok.Builder;

@Builder
public record FeedCommentResponse(
        Long commentId,
        Author author,
        String content,
        String createdAt
) {
    public static FeedCommentResponse from(FeedComment feedComment) {
        return FeedCommentResponse.builder()
                .commentId(feedComment.getId())
                .author(Author.from(feedComment.getAuthor()))
                .content(feedComment.getContent())
                .createdAt(feedComment.getCreatedAt().toString())
                .build();
    }
}
