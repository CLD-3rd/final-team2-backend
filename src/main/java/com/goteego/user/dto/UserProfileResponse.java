package com.goteego.user.dto;

import com.goteego.badge.dto.BadgeResponse;
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
        List<String> travelTags
) {
    public static UserProfileResponse from(User user, int reviewCount, double averageRating) {
        return UserProfileResponse.builder()
                .user(UserResponse.from(user))
                .reviewCount(reviewCount)
                .averageRating(averageRating)

                .build();
    }
}
