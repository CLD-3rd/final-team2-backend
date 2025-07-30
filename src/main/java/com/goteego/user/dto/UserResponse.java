package com.goteego.user.dto;


import com.goteego.user.domain.User;

public record UserResponse(
        Long userId,
        String nickname,
        String profileImgUrl,
        String email

) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getProfileImgUrl(), user.getOauthInfo().getOauthEmail());
    }
}
