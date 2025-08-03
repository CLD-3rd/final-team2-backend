package com.goteego.chat.dto.message;

import com.goteego.chat.domain.enumerate.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private String title;       // 알림 제목 (예: "새 메시지")
    private String content;     // 알림 내용 (메시지 내용)
    private Long senderId;      // 발신자 ID
    private String senderName;  // 발신자 이름
    private String roomId;      // 채팅방 ID
    private LocalDateTime timestamp; // 추가된 내용
    private MessageType type;

    public NotificationResponse() {
    }

    private NotificationResponse(String title, String content, Long senderId, String senderName, String roomId, LocalDateTime timestamp, MessageType type) {
        this.title = title;
        this.content = content;
        this.senderId = senderId;
        this.senderName = senderName;
        this.roomId = roomId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public static NotificationResponse create(String title, String content, Long senderId, String senderName, String roomId, MessageType type) {
        return new NotificationResponse(title, content, senderId, senderName, roomId, LocalDateTime.now(), type);
    }

}
