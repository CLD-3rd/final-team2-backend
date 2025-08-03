package com.goteego.chat.dto.chatroom;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DirectChatRoomDto {
    private String roomId;
    private ChatType type;
    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserEmail;

    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;
    private int unreadCount;

    private DirectChatRoomDto(String roomId, ChatType type, Long otherUserId, String otherUserNickname, String otherUserEmail, String lastMessage, LocalDateTime lastMessageTimestamp, int unreadCount) {
        this.roomId = roomId;
        this.type = type;
        this.otherUserId = otherUserId;
        this.otherUserNickname = otherUserNickname;
        this.otherUserEmail = otherUserEmail;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // 엔티티 → DTO 변환 생성자
    public static DirectChatRoomDto fromEntity(ChatRoom room, Long currentUserId, int unreadCount) {
        User otherUser = getOtherUser(room, currentUserId);
        return new DirectChatRoomDto(
                room.getRoomId(),
                room.getType(),
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getOauthInfo().getOauthEmail(),
                room.getLastMessage(),
                room.getLastMessageTimestamp(),
                unreadCount
        );
    }

    // 상대방 추출 헬퍼 메서드
    private static User getOtherUser(ChatRoom room, Long currentUserId) {
        return room.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND))
                .getUser();
    }
}
