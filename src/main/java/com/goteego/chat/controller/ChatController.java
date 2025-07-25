package com.goteego.chat.controller;


import com.goteego.chat.dto.message.DirectMessageRequest;
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
        log.info("principal name = {}", principal.getName());
        Long senderId = Long.parseLong(principal.getName()); // principal.getName()이 현재 userId로 설정되어 있음 (StompHandler 참고)
        chatService.sendDirectMessage(roomId, directMessageRequest, senderId);
    }

    // 그룹 채팅
//    @MessageMapping("/chat.group.send/{roomId}")
//    @SendTo("/topic/public/{roomId}")
//    public ChatMessageDto sendGroupMessage(@Payload ChatMessageDto chatMessageDto, @DestinationVariable String roomId) {
//        chatMessageDto.setRoomId(roomId);
//        chatMessageDto.setTimestamp(LocalDateTime.now());
//        // 메시지 저장 로직 추가
//        chatService.saveMessage(chatMessageDto);
//        return chatMessageDto;
//    }

    /**
     * 입장 메시지
     */
//    @MessageMapping("/chat.addUser/{roomId}")
//    @SendTo("/topic/public/{roomId}")
//    public ChatMessageDto addUser(@Payload ChatMessageDto chatMessageDto, SimpMessageHeaderAccessor headerAccessor, @DestinationVariable String roomId) {
//        // "session" 헤더에 사용자 이름 추가 (WebSocket 연결 시 설정했다고 가정)
//        // 여기서는 DTO에 담겨온 senderId와 sender 이름을 신뢰하고 사용
//        chatMessageDto.setRoomId(roomId);
//        chatMessageDto.setType(MessageType.ENTER);
//        chatMessageDto.setTimestamp(LocalDateTime.now());
//        chatMessageDto.setContent(chatMessageDto.getSender() + "님이 입장했습니다.");
//
////        chatService.validateUserCanJoinGroupChat(roomId, chatMessageDto.getSenderId());
//        chatService.saveMessage(chatMessageDto);
//        return chatMessageDto;
//    }


}