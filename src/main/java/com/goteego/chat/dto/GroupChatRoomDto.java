package com.goteego.chat.dto;

import com.goteego.chat.domain.enumerate.ChatType;
import lombok.Data;

import java.util.List;

@Data
public class GroupChatRoomDto {
    private String roomId;
    private ChatType type;
    private List<UserChatRoomDto> participants;
    private String groupName;
    private int unreadCount;
}
