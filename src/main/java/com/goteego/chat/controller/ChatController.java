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
import java.util.Map;

//@Controller
//@RequiredArgsConstructor
//@Slf4j
//public class ChatController {
//
//    private final ChatService chatService;
//
//    // 1:1 채팅
//    @MessageMapping("/chat.direct.send/{roomId}")
//    public void sendDirectMessage(@Payload DirectMessageRequest directMessageRequest,
//                                  @DestinationVariable(value = "roomId") String roomId,
//                                  Principal principal) {
//
//        log.info("✅✅✅ [ChatController] /chat.direct.send/{roomId} 메서드 진입! ✅✅✅");
//        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
//        chatService.sendDirectMessage(roomId, directMessageRequest, user.getId());
//
//        Long userId = user.getId();
//        log.warn("메시지 발신자 ID: {}", userId);
//
//        chatService.sendDirectMessage(roomId, directMessageRequest, userId);
//    }
//
//    // 그룹 채팅
//    @MessageMapping("/chat.group.send/{roomId}")
//    public void sendGroupMessage(@Payload GroupMessageRequest groupMessageRequest,
//                                 @DestinationVariable(value = "roomId") String roomId,
//                                 Principal principal) {
//        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
//        chatService.sendGroupMessage(roomId, groupMessageRequest, user.getId());
//        Long userId = user.getId();
//
//        chatService.sendGroupMessage(roomId, groupMessageRequest, userId);
//
//    }
//
//}



@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.direct.send/{roomId}")
    public void sendDirectMessage(
            @Payload DirectMessageRequest directMessageRequest,
            @DestinationVariable(value = "roomId") String roomId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object userIdObj = sessionAttributes.get("userId");

        if (userIdObj == null) {
            log.error("!!!!!!!!!! [ChatController] Session attributes에서 userId를 찾을 수 없습니다. !!!!!!!!!!!");
            return;
        }

        Long userId = (Long) userIdObj;
        log.warn("메시지 발신자 ID: {}", userId);

        chatService.sendDirectMessage(roomId, directMessageRequest, userId);
    }

    @MessageMapping("/chat.group.send/{roomId}")
    public void sendGroupMessage(
            @Payload GroupMessageRequest groupMessageRequest,
            @DestinationVariable(value = "roomId") String roomId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object userIdObj = sessionAttributes.get("userId");

        if (userIdObj == null) {
            log.error("!!!!!!!!!! [ChatController] Session attributes에서 userId를 찾을 수 없습니다. !!!!!!!!!!!");
            return;
        }

        Long userId = (Long) userIdObj;
        log.warn("그룹 메시지 발신자 ID: {}", userId);

        chatService.sendGroupMessage(roomId, groupMessageRequest, userId);
    }
}