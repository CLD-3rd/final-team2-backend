package com.goteego.feed.dto.response;

import com.goteego.feed.domain.Feed;
import com.goteego.global.dto.Author;
import lombok.Builder;

import java.util.List;

@Builder
public record FeedDetailResponse(
        Long feedId,
        Author author,
        String title,
        String content,
        String imageUrl,
        String location,
        Long viewCount,
        String createdAt,
        Boolean badgeRequest,
        List<FeedCommentResponse> comments
) {
    public static FeedDetailResponse from(Feed feed, List<FeedCommentResponse> comments) {
        return FeedDetailResponse.builder()
                .feedId(feed.getId())
                .author(Author.from(feed.getAuthor()))
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation().name())
                .viewCount(feed.getViewCount())
                .createdAt(feed.getCreatedAt().toString())
                .badgeRequest(feed.getBadgeRequest())
                .comments(comments)
                .build();
    }
}
