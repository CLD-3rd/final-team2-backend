package com.goteego.chat.controller;


import com.goteego.chat.domain.enumerate.MessageType;
import com.goteego.chat.dto.ChatMessageDto;
import com.goteego.chat.service.ChatService;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * [전체 흐름]
 *
 * [1] 클라이언트 -> 서버
 * ㄴ 1. WebSocketConfig의 configureMessageBroker를 보면 prefix로 app을 설정
 * ㄴ 2. 클라이언트(js)에서는 /app/chat.sendMessage/room123으로 메시지 전달
 * ㄴ 3. "/app" 접두사를 제거한 뒤에 나머지 경로에 대해 @MessageMapping을 통해 매핑 수행
 *      ㄴ 즉, "app/chat.sendMessage/room123" -> /chat.sendMessage/{roomId}로 매핑
 *
 * [1] 서버 -> 클라이언트
 * ㄴ @SendTo를 통해 /topic/public/{roomId}를 구독 중인 클라이언트에게 메시지를 broadcast하게 전달
 */

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * 1:1 채팅
     */
    @Transactional
    @MessageMapping("/chat.direct.send/{roomId}")
    public void sendDirectMessage(@Payload ChatMessageDto chatMessageDto, @DestinationVariable String roomId,
                                            Principal principal) {

        log.info("principal name = {}", principal.getName());

        Long userId = Long.parseLong(principal.getName()); // 위에서 setUser에 ID 넣었기 때문

        User sender = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("USER NOT FOUND"));

        log.info("sender id = {}", sender.getId());
        log.info("sender email = {}", sender.getOauthInfo().getOauthEmail());

        chatMessageDto.setSenderId(sender.getId());
        chatMessageDto.setSender(sender.getNickname());
        chatMessageDto.setRoomId(roomId);
        chatMessageDto.setTimestamp(LocalDateTime.now());

        User recipient = userRepository.findByOauthInfo_OauthEmail(chatMessageDto.getRecipientEmail())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 메시지 저장 로직 추가
        chatService.saveMessage(chatMessageDto);

        // 1. 수신자에게 메시지 전송
        // /queue/messages는 내부적으로 /user/{recipientPrincipalName}/queue/messages로 변경
        messagingTemplate.convertAndSendToUser(String.valueOf(recipient.getId()), "/queue/messages", chatMessageDto);

        // 2. 발신자에게 메시지 전송 (1:1 채팅이라서 아래를 수행하지 않으면 내가 보낸 메시지를 채팅창에서 확인할 수 없음)
        messagingTemplate.convertAndSendToUser(String.valueOf(sender.getId()), "/queue/messages", chatMessageDto);

    }

    /**
     * 그룹 채팅
     */
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