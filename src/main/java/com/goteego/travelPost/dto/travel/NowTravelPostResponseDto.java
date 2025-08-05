package com.goteego.travelPost.dto.travel;

import com.goteego.profileAnswer.dto.TravelTagResponse;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private AuthorDetail author;
    private String createdAt;

    public static NowTravelPostResponseDto from(TravelPost travelPost, User user,
                                                List<TravelTagResponse> travelTags) {
        return NowTravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .location(travelPost.getLocation().name())
                .author(AuthorDetail.from(user, travelTags))
                .createdAt(travelPost.getCreatedAt().toString())
                .build();
    }
} 