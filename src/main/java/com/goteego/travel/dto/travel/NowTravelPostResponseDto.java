package com.goteego.travel.dto.travel;

import com.goteego.travel.domain.TravelPost;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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
    private Double similarity; // 유사도 점수
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
        private Double rating;
        private List<String> tags;
    }
    
    /**
     * TravelPost 엔티티를 DTO로 변환
     */
    public static NowTravelPostResponseDto from(TravelPost travelPost, Long currentUserId, String nickname, Double similarity) {
        return NowTravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .location(travelPost.getLocation())
                .author(createAuthorDto(travelPost.getUser(), nickname))
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
                .rating(null) // TODO: 실제 평점 시스템 구현 필요
                .tags(List.of()) // TODO: 실제 태그 시스템 구현 필요
                .build();
    }
} 