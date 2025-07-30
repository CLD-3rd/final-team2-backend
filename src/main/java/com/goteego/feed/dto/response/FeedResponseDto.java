package com.goteego.feed.dto.response;

import com.goteego.feed.domain.Feed;
import lombok.Builder;
import lombok.Getter;

/**
 * 피드 응답 DTO 클래스
 * 클라이언트에게 피드 정보를 전달할 때 사용되는 데이터 전송 객체
 */
@Getter
@Builder
public class FeedResponseDto {

    private Long feed_id;
    private AuthorDto author;
    private String title;
    private String content;
    private String image_url;
    private String location;
    private Long view_count;
    private Long like_count;
    private String created_at;
    private String modified_at;
    private Boolean badge_request;

    /**
     * Feed 엔티티를 FeedResponseDto로 변환하는 정적 팩토리 메서드
     */
    public static FeedResponseDto from(Feed feed) {
        return FeedResponseDto.builder()
                .feed_id(feed.getId())
                .author(AuthorDto.builder()
                        .userId(feed.getAuthor().getId())
                        .nickname(feed.getAuthor().getNickname())
                        .profileImageUrl(feed.getAuthor().getProfileImgUrl())
                        .build())
                .title(feed.getTitle())
                .content(feed.getContent())
                .image_url(feed.getImageUrl())
                .location(feed.getLocation() != null ? feed.getLocation().name() : null)
                .view_count(feed.getViewCount())
                .created_at(feed.getCreatedAt().toLocalDate().toString())
                .badge_request(feed.getBadgeRequest())
                .build();
    }

    /**
     * 피드 작성자 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String profileImageUrl;
    }
} 