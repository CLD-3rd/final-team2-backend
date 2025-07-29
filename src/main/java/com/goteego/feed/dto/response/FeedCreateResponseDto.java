package com.goteego.feed.dto.response;

import com.goteego.feed.domain.Feed;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 피드 생성 응답 DTO 클래스
 * 피드 생성 시 클라이언트에게 전달하는 데이터 전송 객체
 */
@Getter
@Builder
public class FeedCreateResponseDto {
    
    private Long feed_id;
    private String title;
    private String content;
    private String image_url;
    private String location;
    private String created_at;
    private Boolean badge_request;
    
    /**
     * Feed 엔티티를 FeedCreateResponseDto로 변환하는 정적 팩토리 메서드
     */
    public static FeedCreateResponseDto from(Feed feed) {
        return FeedCreateResponseDto.builder()
                .feed_id(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .image_url(feed.getImageUrl())
                .location(feed.getLocation() != null ? feed.getLocation().name() : null)
                .created_at(feed.getCreatedAt().toLocalDate().toString())
                .badge_request(feed.getBadgeRequest())
                .build();
    }
} 