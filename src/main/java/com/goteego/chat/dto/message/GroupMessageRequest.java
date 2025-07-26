package com.goteego.chat.dto.message;

import com.goteego.chat.domain.enumerate.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageRequest {
    private String content;
    private MessageType type;

    public GroupMessageRequest(String content) {
        this.content = content;
        this.type = MessageType.TALK; // 기본값 설정
    }
}
