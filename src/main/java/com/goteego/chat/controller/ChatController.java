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
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    public void sendDirectMessage(@Payload DirectMessageRequest directMessageRequest,
                                  @DestinationVariable(value = "roomId") String roomId,
//                                  Principal principal,
                                  SimpMessageHeaderAccessor headerAccessor) {

//        log.info("✅✅✅ [ChatController] /chat.direct.send/{roomId} 메서드 진입! ✅✅✅");
//        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
//        chatService.sendDirectMessage(roomId, directMessageRequest, user.getId());

        // 1. headerAccessor에서 인증 토큰을 가져옵니다.
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();

        // 2. 토큰이 null인지 반드시 확인합니다. (안정성)
        if (token == null) {
            log.error("!!!!!!!!!! [ChatController] headerAccessor에서 사용자 정보를 찾을 수 없습니다. !!!!!!!!!!!");
            return; // 또는 예외 처리
        }

        // 3. 토큰에서 Principal(User 객체)을 꺼냅니다.
        User user = (User) token.getPrincipal();

        // 4. User 객체에서 ID를 안전하게 가져옵니다.
        Long userId = user.getId();
        log.warn("메시지 발신자 ID: {}", userId);

        chatService.sendDirectMessage(roomId, directMessageRequest, userId);
    }

    // 그룹 채팅
    @MessageMapping("/chat.group.send/{roomId}")
    public void sendGroupMessage(@Payload GroupMessageRequest groupMessageRequest,
                                 @DestinationVariable(value = "roomId") String roomId,
//                                 Principal principal,
                                 SimpMessageHeaderAccessor headerAccessor) {
//        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
//        chatService.sendGroupMessage(roomId, groupMessageRequest, user.getId());

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        if (token == null) {
            log.error("!!!!!!!!!! [ChatController] headerAccessor에서 사용자 정보를 찾을 수 없습니다. !!!!!!!!!!!");
            return;
        }
        User user = (User) token.getPrincipal();
        Long userId = user.getId();
        log.warn("그룹 메시지 발신자 ID: {}", userId);

        chatService.sendGroupMessage(roomId, groupMessageRequest, userId);

    }

}