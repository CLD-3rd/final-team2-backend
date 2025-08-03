package com.goteego.travelPost.dto.travel;

import com.goteego.travelPost.domain.TravelPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 현지동행모집글 응답 DTO (NOW)
 * API 응답 시 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NowTravelPostResponseDto {

    private Long travelPostId;
    private String title;
    private String location;
    private AuthorDto author;
    private String createdAt;

    /**
     * TravelPost 엔티티를 DTO로 변환
     */
    public static NowTravelPostResponseDto from(TravelPost travelPost, String nickname) {
        return NowTravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .location(travelPost.getLocation().name())
                .author(createAuthorDto(travelPost.getUser(), nickname))
                .createdAt(travelPost.getCreatedAt().toString())
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
                // TODO: user_review 테이블 구현 후 추가
                // .rating(null)
                // .tags(List.of())
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
        // TODO: user_review 테이블 구현 후 추가
        // private Double rating;
        // private List<String> tags;
    }
} 