package com.goteego.feed.dto.response;

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
    private Long comment_id;
    
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
    private String created_at;
    
    /**
     * 코멘트 수정 날짜 (시간 정보 제외)
     */
    private String modified_at;
    
    /**
     * 코멘트 작성자 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class AuthorDto {
        /**
         * 작성자 사용자 ID
         */
        private Long user_id;
        
        /**
         * 작성자 닉네임
         */
        private String nickname;
    }
    
    /**
     * FeedComment 엔티티를 FeedCommentResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param comment 변환할 FeedComment 엔티티
     * @return 변환된 FeedCommentResponseDto 객체
     */
    public static FeedCommentResponseDto from(FeedComment comment) {
        return FeedCommentResponseDto.builder()
                .comment_id(comment.getId())
                .author(AuthorDto.builder()
                        .user_id(comment.getAuthor().getId())
                        .nickname(comment.getAuthor().getNickname())
                        .build())
                .content(comment.getContent())
                .created_at(comment.getCreatedAt().toLocalDate().toString())
                .modified_at(comment.getModifiedAt() != null ? comment.getModifiedAt().toLocalDate().toString() : comment.getCreatedAt().toLocalDate().toString())
                .build();
    }
} 