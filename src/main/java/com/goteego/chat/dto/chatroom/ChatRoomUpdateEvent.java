package com.goteego.chat.dto.chatroom;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomUpdateEvent {
    private String roomId;          // 채팅방 UUID
    private String roomName;       // 채팅방 이름
    private String lastMessage;    // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 메시지 시간
    private String senderName;     // 보낸 사람 닉네임
    private boolean isGroup;       // 그룹 채팅 여부
    private Long otherUserId;      // 개인 채팅시 상대방 ID 추가
}