package com.goteego.feed.dto.response;

import com.goteego.feed.domain.Feed;
import com.goteego.global.dto.Author;
import lombok.Builder;

@Builder
public record FeedResponse(
        Long feedId,
        Author author,
        String title,
        String content,
        String imageUrl,
        String location,
        Long viewCount,
        String createdAt
) {
    public static FeedResponse from(Feed feed) {
        return FeedResponse.builder()
                .feedId(feed.getId())
                .author(Author.from(feed.getAuthor()))
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation().name())
                .viewCount(feed.getViewCount())
                .createdAt(feed.getCreatedAt().toString())
                .build();
    }
}
