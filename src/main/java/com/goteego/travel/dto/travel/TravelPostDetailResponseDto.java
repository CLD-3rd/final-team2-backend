package com.goteego.travel.dto.travel;

import com.goteego.travel.domain.TravelPost;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPostDetailResponseDto {
    private Long travelPostId;
    private String title;
    private String content;
    private String location;
    private String startTime;
    private String endTime;
    private AuthorDto author;
    private Integer maxParticipants;
    private String imageUrl;
    private String createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
    }

    public static TravelPostDetailResponseDto from(TravelPost travelPost) {
        return TravelPostDetailResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .content(travelPost.getContent())
                .location(travelPost.getLocation())
                .startTime(travelPost.getStartTime().toString())
                .endTime(travelPost.getEndTime().toString())
                .author(createAuthorDto(travelPost.getUser()))
                .maxParticipants(travelPost.getRecruitLimit())
                .imageUrl(travelPost.getImageUrl())
                .createdAt(travelPost.getCreatedAt().toString())
                .build();
    }

    private static AuthorDto createAuthorDto(com.goteego.user.domain.User user) {
        return AuthorDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }
} 