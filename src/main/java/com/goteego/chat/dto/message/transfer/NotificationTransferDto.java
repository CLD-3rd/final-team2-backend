package com.goteego.chat.dto.message.transfer;

import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.NotificationResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTransferDto {
    private Long recipientId;
    private NotificationResponse notificationResponse;
}
