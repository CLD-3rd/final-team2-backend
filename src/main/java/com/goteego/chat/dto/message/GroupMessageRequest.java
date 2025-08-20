package com.goteego.chat.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.goteego.chat.domain.enumerate.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupMessageRequest {
    private String content;
    private MessageType type;
}
