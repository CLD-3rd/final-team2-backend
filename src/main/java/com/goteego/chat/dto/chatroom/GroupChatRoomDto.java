package com.goteego.chat.dto.chatroom;

import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.chat.dto.UserChatRoomDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupChatRoomDto {
    private String roomId;
    private ChatType type;
    private List<ChatParticipantsDto> participants;
    private String groupName;
    private int unreadCount;
}
