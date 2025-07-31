package com.goteego.chat.controller;


import com.goteego.chat.dto.message.DirectMessageRequest;
import com.goteego.chat.dto.message.GroupMessageRequest;
import com.goteego.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // 1:1 채팅
    @Transactional
    @MessageMapping("/chat.direct.send/{roomId}")
    public void sendDirectMessage(@Payload DirectMessageRequest directMessageRequest, @DestinationVariable String roomId, Principal principal) {
        Long senderId = getUserId(principal);
        chatService.sendDirectMessage(roomId, directMessageRequest, senderId);
    }

    // 그룹 채팅
    @Transactional
    @MessageMapping("/chat.group.send/{roomId}")
    public void sendDirectMessage(@Payload GroupMessageRequest groupMessageRequest, @DestinationVariable String roomId, Principal principal) {
        Long senderId = getUserId(principal);
        chatService.sendGroupMessage(roomId, groupMessageRequest, senderId);
    }

    private Long getUserId(Principal principal) {
        return Long.parseLong(principal.getName());
    }
}