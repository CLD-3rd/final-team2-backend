package com.goteego.feed.dto;

import com.goteego.feed.domain.Feed;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 피드 응답 DTO 클래스
 * 클라이언트에게 피드 정보를 전달할 때 사용되는 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Builder
public class FeedResponseDto {
    
    /**
     * 피드 고유 식별자
     */
    private Long feedId;
    
    /**
     * 피드 작성자 정보
     */
    private AuthorDto author;
    
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
     * 피드 조회수
     */
    private Long viewCount;
    
    /**
     * 피드 생성 날짜 (시간 정보 제외)
     */
    private LocalDate createdAt;
    
    /**
     * 피드 수정 날짜 (시간 정보 제외)
     */
    private LocalDate modifiedAt;
    
    /**
     * 배지 요청 여부
     */
    private Boolean badgeRequest;
    
    /**
     * 피드 작성자 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class AuthorDto {
        /**
         * 작성자 사용자 ID
         */
        private Long userId;
        
        /**
         * 작성자 닉네임
         */
        private String nickname;
    }
    
    /**
     * Feed 엔티티를 FeedResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param feed 변환할 Feed 엔티티
     * @param authorNickname 작성자 닉네임
     * @return 변환된 FeedResponseDto 객체
     */
    public static FeedResponseDto from(Feed feed, String authorNickname) {
        return FeedResponseDto.builder()
                .feedId(feed.getId())
                .author(AuthorDto.builder()
                        .userId(feed.getUserId())
                        .nickname(authorNickname)
                        .build())
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation())
                .viewCount(feed.getViewCount())
                .createdAt(feed.getCreatedAt().toLocalDate())
                .modifiedAt(feed.getModifiedAt() != null ? feed.getModifiedAt().toLocalDate() : feed.getCreatedAt().toLocalDate())
                .badgeRequest(feed.getBadgeRequest())
                .build();
    }
    
    /**
     * Feed 엔티티를 FeedResponseDto로 변환하는 정적 팩토리 메서드 (수정용)
     * createdAt은 제외하고 modifiedAt만 포함
     * 
     * @param feed 변환할 Feed 엔티티
     * @param authorNickname 작성자 닉네임
     * @return 변환된 FeedResponseDto 객체
     */
    public static FeedResponseDto fromForUpdate(Feed feed, String authorNickname) {
        return FeedResponseDto.builder()
                .feedId(feed.getId())
                .author(AuthorDto.builder()
                        .userId(feed.getUserId())
                        .nickname(authorNickname)
                        .build())
                .title(feed.getTitle())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .location(feed.getLocation())
                .viewCount(feed.getViewCount())
                .modifiedAt(feed.getModifiedAt() != null ? feed.getModifiedAt().toLocalDate() : null)
                .badgeRequest(feed.getBadgeRequest())
                .build();
    }
} 