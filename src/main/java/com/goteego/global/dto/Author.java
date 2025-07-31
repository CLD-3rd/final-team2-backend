package com.goteego.global.dto;

import com.goteego.user.domain.User;
import lombok.Builder;

@Builder
public record Author(
        Long userId,
        String nickname,
        String profileImgUrl
) {
    public static Author from(User user) {
        return Author.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }
}
