package com.goteego.chat.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private String title;       // 알림 제목 (예: "새 메시지")
    private String content;     // 알림 내용 (메시지 내용)
    private Long senderId;      // 발신자 ID
    private String senderName;  // 발신자 이름
    private String roomId;      // 채팅방 ID
}
