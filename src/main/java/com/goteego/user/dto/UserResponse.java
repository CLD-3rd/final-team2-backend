package com.goteego.user.dto;


public record UserResponse(
        Long userId,
        String nickname,
        String profileImgUrl,
        String email

) {
    public static UserResponse of(Long userId, String nickname, String profileImgUrl, String email) {
        return new UserResponse(userId, nickname, profileImgUrl, email);
    }
}
