package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.dto.ChatMessageDto;
import com.goteego.user.domain.User;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.chat.repository.ChatRoomRepository;
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

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    /**
     * 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(messageDto.getRoomId()).orElseThrow();
        User sender = userRepository.findById(messageDto.getSenderId()).orElseThrow();

        ChatMessage chatMessage = ChatMessage.create(
                chatRoom.getRoomId(),
                sender.getId(),
                sender.getNickname(),
                messageDto.getContent(),
                messageDto.getType()
        );

        return chatMessageRepository.save(chatMessage);
    }

    /**
     * 특정 채팅방의 메시지 내역 조회
     */
    public List<ChatMessageDto> findChatMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(m -> new ChatMessageDto(
                        m.getType(),
                        m.getRoomId(),
                        m.getSenderId(),
                        m.getSenderName(),
                        m.getContent(),
                        m.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 1:1 채팅 메시지 전송
     */
//    public void sendMessageToUser(String username, ChatMessageDto messageDto) {
//        messagingTemplate.convertAndSendToUser(username, "/queue/messages", messageDto);
//    }


}
