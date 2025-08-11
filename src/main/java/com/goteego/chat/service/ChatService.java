package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.MessageType;
import com.goteego.chat.dto.message.*;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.user.dto.UserDto;
import com.goteego.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository chatMessageRepository, UserService userService,
                       ChatRoomRepository chatRoomRepository, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.chatRoomRepository = chatRoomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**********************
     * 1:1 채팅 메시지 전송 /
     *********************/
    @Transactional
    public void sendDirectMessage(String roomId, DirectMessageRequest directMessageRequest, Long senderId) {
        UserDto sender = getUser(senderId);
        UserDto recipient = getUser(directMessageRequest.getRecipientId());
        ChatMessage message = createAndSaveMessage(roomId, directMessageRequest.getContent(), directMessageRequest.getType(), sender);
        updateChatRoomLastMessage(roomId, message);

        DirectMessageResponse responseDto = message.toDirectMessageDto();

        // 수신자와 발신자 모두에게 메시지 전송
        messagingTemplate.convertAndSendToUser(String.valueOf(recipient.getId()), "/queue/messages", responseDto);
        messagingTemplate.convertAndSendToUser(String.valueOf(sender.getId()), "/queue/messages", responseDto);

        // 알림 전송 로직 (필요 시)
        sendNotification(message, sender, recipient.getId(), roomId);
    }

    /***********************
     * 그룹 채팅 메시지 전송 /
     **********************/
    @Transactional
    public void sendGroupMessage(String roomId, GroupMessageRequest groupMessageRequest, Long senderId) {
        UserDto sender = getUser(senderId);
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));
        ChatMessage message = createAndSaveMessage(roomId, groupMessageRequest.getContent(), groupMessageRequest.getType(), sender);
        updateChatRoomLastMessage(roomId, message);

        GroupMessageResponse responseDto = message.toGroupMessageDto();

        // 해당 토픽을 구독하는 모든 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, responseDto);

        // 알림 전송 로직
        chatRoom.getParticipants().forEach(participant -> {
            if (!participant.getUser().getId().equals(senderId)) {
                sendNotification(message, sender, participant.getUser().getId(), roomId);
            }
        });
    }


    /********************************
     * 특정 채팅방의 메시지 내역 조회  /
     *******************************/
    public Slice<DirectMessageResponse> findChatMessages(String roomId, Pageable pageable) {
        Slice<ChatMessage> messageSlice = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        return messageSlice.map(DirectMessageResponse::fromEntity);
    }


    //===============================내부로직===============================//

    // 사용자 조회
    private UserDto getUser(Long userId) {
        return userService.getUser(userId);
    }

    // 메시지 저장
    private ChatMessage createAndSaveMessage(String roomId, String content, MessageType type, UserDto sender) {
        return chatMessageRepository.save(ChatMessage.create(roomId, sender.getId(), sender.getNickname(), content, type));
    }

    // 알림 전송
    private void sendNotification(ChatMessage message, UserDto sender, Long recipientId, String roomId) {
        NotificationResponse notification = NotificationResponse.create(sender.getEmail(), message.getContent(),
                sender.getId(), sender.getNickname(), roomId, message.getType());
        messagingTemplate.convertAndSendToUser(String.valueOf(recipientId), "/queue/notifications", notification);
    }

    // 마지막 메시지 업데이트 로직
    private void updateChatRoomLastMessage(String roomId, ChatMessage message) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));
        chatRoom.updateLastMessage(message.getContent(), message.getTimestamp());
    }


}

