package com.goteego.chat.dto.message.transfer;

import com.goteego.chat.dto.message.DirectMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageTransferDto {
    private Long recipientId;
    private DirectMessageResponse messageResponse;
}