package com.goteego.user.dto;

import com.goteego.badge.dto.BadgeResponse;
import com.goteego.profileAnswer.dto.TravelTagResponse;
import com.goteego.user.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record UserProfileResponse(
        UserResponse user,
        int reviewCount,
        double averageRating,
        List<BadgeResponse> displayBadges,
        List<BadgeResponse> ownedBadges,
        List<TravelTagResponse> travelTags
) {
    public static UserProfileResponse from(User user, List<TravelTagResponse> travelTags) {
        return UserProfileResponse.builder()
                .user(UserResponse.from(user))
                .reviewCount(user.getReviewCount())
                .averageRating(user.getAverageRating())
                .travelTags(travelTags)
                .build();
    }
}
