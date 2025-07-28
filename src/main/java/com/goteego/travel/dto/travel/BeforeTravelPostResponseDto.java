package com.goteego.travel.dto.travel;

import com.goteego.travel.domain.TravelPost;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사전동행모집글 응답 DTO (BEFORE)
 * API 응답 시 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeforeTravelPostResponseDto {
    
    private Long travelPostId;
    private String title;
    private String content;
    private String location;
    private Long viewCount;
    private String startTime;
    private String endTime;
    private AuthorDto author;
    private Integer participants; // 현재까지 신청받은 인원 수
    private Integer maxParticipants; // 모집 인원 제한
    private String imageUrl;
    private String createdAt;
    private Double similarity; // 유사도 점수
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
    }
    
    /**
     * TravelPost 엔티티를 DTO로 변환
     */
    public static BeforeTravelPostResponseDto from(TravelPost travelPost, Long currentUserId, String nickname, Double similarity, Integer approvedParticipantCount) {
        return BeforeTravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .content(travelPost.getContent())
                .location(travelPost.getLocation())
                .viewCount(travelPost.getViewCount())
                .startTime(travelPost.getStartTime().toString())
                .endTime(travelPost.getEndTime().toString())
                .author(createAuthorDto(travelPost.getUser(), nickname))
                .participants(approvedParticipantCount != null ? approvedParticipantCount : 0)
                .maxParticipants(travelPost.getRecruitLimit())
                .imageUrl(travelPost.getImageUrl())
                .createdAt(travelPost.getCreatedAt().toString())
                .similarity(similarity)
                .build();
    }

    /**
     * AuthorDto 생성 (객체 참조 방식)
     */
    private static AuthorDto createAuthorDto(com.goteego.user.domain.User user, String nickname) {
        return AuthorDto.builder()
                .userId(user.getId())
                .nickname(nickname)
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }
} 