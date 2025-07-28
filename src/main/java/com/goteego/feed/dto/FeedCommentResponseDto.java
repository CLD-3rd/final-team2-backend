package com.goteego.feed.dto;

import com.goteego.feed.domain.FeedComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 피드 코멘트 응답 DTO 클래스
 * 클라이언트에게 코멘트 정보를 전달할 때 사용되는 데이터 전송 객체
 * 
 * @author GotEEgo Team
 * @version 1.0
 */
@Getter
@Builder
public class FeedCommentResponseDto {
    
    /**
     * 코멘트 고유 식별자
     */
    private Long commentId;
    
    /**
     * 코멘트 작성자 정보
     */
    private AuthorDto author;
    
    /**
     * 코멘트 내용
     */
    private String content;
    
    /**
     * 코멘트 생성 날짜 (시간 정보 제외)
     */
    private LocalDate createdAt;
    
    /**
     * 코멘트 수정 날짜 (시간 정보 제외)
     */
    private LocalDate modifiedAt;
    
    /**
     * 코멘트 작성자 정보를 담는 내부 클래스
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
     * FeedComment 엔티티를 FeedCommentResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param comment 변환할 FeedComment 엔티티
     * @param authorNickname 작성자 닉네임
     * @return 변환된 FeedCommentResponseDto 객체
     */
    public static FeedCommentResponseDto from(FeedComment comment, String authorNickname) {
        return FeedCommentResponseDto.builder()
                .commentId(comment.getId())
                .author(AuthorDto.builder()
                        .userId(comment.getUserId())
                        .nickname(authorNickname)
                        .build())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().toLocalDate())
                .modifiedAt(comment.getModifiedAt() != null ? comment.getModifiedAt().toLocalDate() : comment.getCreatedAt().toLocalDate())
                .build();
    }
} 