package com.goteego.chat.dto;


import com.goteego.chat.domain.enumerate.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private MessageType type;

    // 프론트에서 설정
    private String roomId;
    private String content;
    // 추가 항목
    private Long recipientId;
    private String recipientEmail;

    // 서버에서 설정
    private Long senderId;
    private String sender;
    private LocalDateTime timestamp;

    public ChatMessageDto() {
    }

    public ChatMessageDto(MessageType type, String roomId, Long senderId, String sender, String content, LocalDateTime timestamp) {
        this.type = type;
        this.roomId = roomId;
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ChatMessageDto(MessageType type, String roomId, String content, Long recipientId,
                          String recipientEmail, Long senderId, String sender, LocalDateTime timestamp) {
        this.type = type;
        this.roomId = roomId;
        this.content = content;
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.senderId = senderId;
        this.sender = sender;
        this.timestamp = timestamp;
    }
}