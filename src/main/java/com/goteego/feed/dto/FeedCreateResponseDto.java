package com.goteego.feed.dto;

import com.goteego.feed.domain.Feed;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 피드 생성 응답 DTO 클래스
 * 피드 생성 시 클라이언트에게 전달하는 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Builder
public class FeedCreateResponseDto {
    
    /**
     * 생성된 피드의 고유 ID
     */
    private Long feedId;
    
    /**
     * 피드 제목
     */
    private String title;
    
    /**
     * 피드 내용
     */
    private String content;
    
    /**
     * 피드 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 여행 위치 정보
     */
    private String location;
    
    /**
     * 피드 생성 날짜
     */
    private LocalDate createdAt;
    
    /**
     * 배지 요청 여부
     */
    private Boolean badgeRequest;
    
    /**
     * Feed 엔티티를 FeedCreateResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param feed 변환할 Feed 엔티티
     * @return 변환된 FeedCreateResponseDto 객체
     */
    public static FeedCreateResponseDto from(Feed feed) {
        return FeedCreateResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation())
                .createdAt(feed.getCreatedAt().toLocalDate())
                .badgeRequest(feed.getBadgeRequest())
                .build();
    }
} 