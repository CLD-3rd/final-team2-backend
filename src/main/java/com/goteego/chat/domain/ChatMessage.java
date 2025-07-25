package com.goteego.chat.domain;

import com.goteego.chat.domain.enumerate.MessageType;
import com.goteego.chat.dto.message.DirectMessageResponse;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    private String id;  // MongoDB의 ObjectId

    private String roomId; // UUID

    private Long senderId;
    private String senderName;

    private String content;

    private MessageType type;

    private LocalDateTime timestamp;

    public static ChatMessage create(String roomId, Long senderId, String senderName, String content, MessageType type) {
        ChatMessage message = new ChatMessage();
        message.roomId = roomId;
        message.senderId = senderId;
        message.senderName = senderName;
        message.content = content;
        message.type = type;
        message.timestamp = LocalDateTime.now();
        return message;
    }

    public DirectMessageResponse toDto() {
        return new DirectMessageResponse(
                this.roomId,
                this.senderId,
                this.senderName,
                this.content,
                this.timestamp,
                this.type
                );
    }

}
