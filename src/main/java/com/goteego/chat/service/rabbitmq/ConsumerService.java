package com.goteego.chat.service.rabbitmq;

import com.goteego.chat.dto.message.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

    @RabbitListener(queues = "hello.queue")
    public void receiveMessage(MessageDto messageDto) {
        log.debug("메시지 수신 성공 :: Title: {}, Message: {}", messageDto.getTitle(), messageDto.getMessage());
    }
}