package com.goteego.feed.dto.response;

import com.goteego.feed.domain.Feed;
import com.goteego.global.domain.enumerate.Location;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 피드 상세 응답 DTO 클래스
 * 피드 상세 정보와 코멘트 목록을 포함하여 클라이언트에게 전달하는 데이터 전송 객체
 */
@Getter
@Builder
public class FeedDetailResponseDto {

    private Long feedId;
    private Long userId;
    private String title;
    private String content;
    private String imageUrl;
    private Location location;
    private Boolean badgeRequest;
    private Long viewCount;
    private Long likeCount;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
    private AuthorDto author;
    private List<FeedCommentResponseDto> comments;

    /**
     * Feed 엔티티를 FeedDetailResponseDto로 변환하는 정적 팩토리 메서드
     */
    public static FeedDetailResponseDto from(Feed feed, List<FeedCommentResponseDto> comments) {
        return FeedDetailResponseDto.builder()
                .feedId(feed.getId())
                .userId(feed.getAuthor().getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation())
                .badgeRequest(feed.getBadgeRequest())
                .viewCount(feed.getViewCount())
                .createdAt(feed.getCreatedAt().toLocalDate())
                .author(AuthorDto.builder()
                        .nickname(feed.getAuthor().getNickname())
                        .profileImage(feed.getAuthor().getProfileImgUrl())
                        .build())
                .comments(comments)
                .build();
    }

    /**
     * 피드 작성자 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class AuthorDto {
        private String nickname;
        private String profileImage;
    }
} 