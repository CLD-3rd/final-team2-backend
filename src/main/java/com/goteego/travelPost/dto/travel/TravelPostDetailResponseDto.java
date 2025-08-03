package com.goteego.travelPost.dto.travel;

import com.goteego.global.domain.enumerate.Location;
import com.goteego.travelPost.domain.TravelPost;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPostDetailResponseDto {
    private Long travelPostId;
    private String title;
    private String content;
    private Location location;
    private String startTime;
    private String endTime;
    private AuthorDto author;
    private Integer maxParticipants;
    private String imageUrl;
    private String createdAt;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
    }
} 