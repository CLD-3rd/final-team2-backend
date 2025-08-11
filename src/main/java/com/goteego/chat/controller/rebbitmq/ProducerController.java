package com.goteego.chat.controller.rebbitmq;

import com.goteego.chat.dto.message.MessageDto;
import com.goteego.chat.service.rabbitmq.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final ProducerService producerService;

    /**
     * 생산자(Proceduer)가 메시지를 전송
     */
    @PostMapping("/send")
    public void sendMessage(@RequestBody MessageDto messageDto) {
        producerService.sendMessage(messageDto);
    }
}
