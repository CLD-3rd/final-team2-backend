package com.goteego.chat.controller;


import com.goteego.chat.dto.message.DirectMessageRequest;
import com.goteego.chat.dto.message.GroupMessageRequest;
import com.goteego.chat.service.ChatService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // 1:1 채팅
    @MessageMapping("/chat.direct.send/{roomId}")
    public void sendDirectMessage(@Payload DirectMessageRequest directMessageRequest, @DestinationVariable(value = "roomId") String roomId, Principal principal) {
        log.info("principal getName = {}", principal.getName());
        log.info("principal = {}", principal);
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        log.info("senderId = {}", user.getId());
        chatService.sendDirectMessage(roomId, directMessageRequest, user.getId());
    }

    // 그룹 채팅
    @MessageMapping("/chat.group.send/{roomId}")
    public void sendGroupMessage(@Payload GroupMessageRequest groupMessageRequest, @DestinationVariable(value = "roomId") String roomId, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        chatService.sendGroupMessage(roomId, groupMessageRequest, user.getId());
    }

}