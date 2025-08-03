package com.goteego.chat.dto.chatroom;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data

public class GroupChatRoomDto {
    private String roomId;
    private ChatType type;
    private List<ChatParticipantsDto> participants;
    private String groupName;

    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;
    private int unreadCount;
    @Builder
    private GroupChatRoomDto(String roomId, ChatType type, List<ChatParticipantsDto> participants, String groupName, int unreadCount, String lastMessage, LocalDateTime lastMessageTimestamp) {
        this.roomId = roomId;
        this.type = type;
        this.participants = participants;
        this.groupName = groupName;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // 엔티티 → DTO 변환 생성자
    public static GroupChatRoomDto fromEntity(ChatRoom room, int unreadCount) {
        return new GroupChatRoomDto(
                room.getRoomId(),
                ChatType.GROUP,
                room.getParticipants().stream()
                                .map(ChatParticipantsDto::from)
                                        .collect(Collectors.toList()),
                room.getName(),
                unreadCount,
                room.getLastMessage(),
                room.getLastMessageTimestamp()
        );
    }
}
