package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.dto.message.DirectMessageRequest;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 1:1 채팅 메시지 전송
     */
    @Transactional
    public void sendDirectMessage(String roomId, DirectMessageRequest directMessageRequest, Long senderId) {
        // 1. 발신자 조회
        User sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalStateException("USER NOT FOUND"));
        // 2. 수신자 조회
        User recipient = userRepository.findById(directMessageRequest.getRecipientId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        log.info("sender id = {}", sender.getId());
        log.info("recipient id = {}", recipient.getId());

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.create(
                        roomId,
                        sender.getId(),
                        sender.getNickname(),
                        directMessageRequest.getContent(),
                        directMessageRequest.getType()
                )
        );

        // 3. 메시지 전송
        sendToEachOther(message.toDto(), sender.getId(), recipient.getId());

    }

    private void sendToEachOther(DirectMessageResponse message, Long senderId, Long recipientId) {
        messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/messages", message);
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", message);
    }

    /**
     * 특정 채팅방의 메시지 내역 조회
     */
    public List<DirectMessageResponse> findChatMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(DirectMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }



}
