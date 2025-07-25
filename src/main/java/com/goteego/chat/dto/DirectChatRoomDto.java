package com.goteego.chat.dto;

import com.goteego.chat.domain.enumerate.ChatType;
import lombok.Data;

@Data
public class DirectChatRoomDto {
    private String roomId;
    private String name;
    private ChatType type;
    private Long otherUserId;
    private String otherUserEmail;

    public DirectChatRoomDto(String roomId, String otherUserNickname, ChatType type,
                             Long otherUserId, String otherUserEmail) {
        this.roomId = roomId;
        this.name = otherUserNickname; // or this.otherUserNickname
        this.type = type;
        this.otherUserId = otherUserId;
        this.otherUserEmail = otherUserEmail;
    }

}
