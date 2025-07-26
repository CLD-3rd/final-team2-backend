package com.goteego.travel.dto.travel;

import com.goteego.travel.domain.TravelPost;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 게시글 응답 DTO
 * API 응답 시 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPostResponseDto {
    
    private Long travelPostId;
    private String title;
    private Long userId;
    private String nickname;
    private LocalDate startTime;
    private LocalDate endTime;
    private Integer recruitLimit;
    private Long viewCount;
    private Boolean isAddRecruit;
    private LocalDateTime createdAt;
    private Double similarity; // 유사도 점수
    
    /**
     * TravelPost 엔티티를 DTO로 변환
     */
    public static TravelPostResponseDto from(TravelPost travelPost, Long currentUserId, String nickname, Double similarity) {
        return TravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .userId(travelPost.getUser().getId())
                .nickname(nickname)
                .startTime(travelPost.getStartTime())
                .endTime(travelPost.getEndTime())
                .recruitLimit(travelPost.getRecruitLimit())
                .viewCount(travelPost.getViewCount())
                .isAddRecruit(travelPost.getIsAddRecruit())
                .createdAt(travelPost.getCreatedAt())
                .similarity(similarity)
                .build();
    }
} 