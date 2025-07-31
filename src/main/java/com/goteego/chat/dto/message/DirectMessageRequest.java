package com.goteego.chat.dto.message;

import com.goteego.chat.domain.enumerate.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageRequest {
    private String content;          // 필수: 메시지 내용
    private Long recipientId;      // 필수: 수신자 아이디
    private MessageType type;        // 선택: 기본값 "TALK"
}