package com.goteego.chat.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserChatRoomDto {

    private Long userId;
    private String nickname;
    private LocalDateTime lastReadAt;
    // private Role role // 이거 추가할지 안할지 모르겠음


    public UserChatRoomDto(Long userId, String nickname, LocalDateTime lastReadAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.lastReadAt = lastReadAt;
    }

}
