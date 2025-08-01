package com.goteego.chat.dto.message;

import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.enumerate.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectMessageResponse {
    private String roomId;           // 채팅방 ID
    private Long senderId;           // 발신자 ID
    private String senderName;       // 발신자 닉네임
    private String content;          // 메시지 내용
    private LocalDateTime timestamp; // 발송 시간
    private MessageType type;        // 메시지 타입


    // 엔티티 → DTO 변환
    public static DirectMessageResponse fromEntity(ChatMessage message) {
        return new DirectMessageResponse(
                message.getRoomId(),
                message.getSenderId(),
                message.getSenderName(),
                message.getContent(),
                message.getTimestamp(),
                message.getType()
        );
    }
}
