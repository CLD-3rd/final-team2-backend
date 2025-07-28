package com.goteego.feed.dto;

import com.goteego.feed.domain.Feed;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 피드 상세 응답 DTO 클래스
 * 피드 상세 정보와 코멘트 목록을 포함하여 클라이언트에게 전달하는 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Builder
public class FeedDetailResponseDto {
    
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
     * 피드에 달린 코멘트 목록
     */
    private List<FeedCommentResponseDto> comments;
    
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
     * Feed 엔티티와 코멘트 목록을 FeedDetailResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param feed 변환할 Feed 엔티티
     * @param authorNickname 작성자 닉네임
     * @param comments 코멘트 목록
     * @return 변환된 FeedDetailResponseDto 객체
     */
    public static FeedDetailResponseDto from(Feed feed, String authorNickname, List<FeedCommentResponseDto> comments) {
        return FeedDetailResponseDto.builder()
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
                .comments(comments)
                .build();
    }
} 