package com.goteego.travelPost.dto.travel;

import com.goteego.profileAnswer.dto.TravelTagResponse;
import com.goteego.user.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record AuthorDetail(
        Long userId,
        String nickname,
        String profileImgUrl,
        double averageRating,
        List<TravelTagResponse> travelTags
) {
    public static AuthorDetail from(User user, List<TravelTagResponse> travelTags) {
        return AuthorDetail.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .averageRating(user.getAverageRating())
                .travelTags(travelTags)
                .build();
    }
}
