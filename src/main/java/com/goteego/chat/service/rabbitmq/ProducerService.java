package com.goteego.chat.service.rabbitmq;

import com.goteego.chat.dto.message.MessageDto;

public interface ProducerService {
    void sendMessage(MessageDto messageDto);
}
