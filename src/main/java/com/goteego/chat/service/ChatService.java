package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.MessageType;
import com.goteego.chat.dto.message.*;
import com.goteego.chat.dto.message.transfer.DirectMessageTransferDto;
import com.goteego.chat.dto.message.transfer.NotificationTransferDto;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.chat.service.redis.RedisPublisher;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.user.dto.UserDto;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisPublisher redisPublisher;
    private final ChannelTopic chatTopic;
    private final ChannelTopic notificationTopic;

    /**********************
     * 1:1 채팅 메시지 전송 /
     *********************/
    @Transactional
    public void sendDirectMessage(String roomId, DirectMessageRequest directMessageRequest, Long senderId) {

        // 1. 발신자 조회
        UserDto sender = getUser(senderId);

        // 2. 수신자 조회
        UserDto recipient = getUser(directMessageRequest.getRecipientId());

        // 3. 메시지 DB 저장
        ChatMessage message = createAndSaveMessage(roomId, directMessageRequest.getContent(), directMessageRequest.getType(), sender);

        // 4. 메시지 전송 (수신자와 발신자 모두에게 전송)
        sendDirectMessage(message, recipient.getId());

        // 5. 알림 전송
        sendNotification(message, sender, recipient.getId(), roomId);
    }

    /***********************
     * 그룹 채팅 메시지 전송 /
     **********************/
    @Transactional
    public void sendGroupMessage(String roomId, GroupMessageRequest groupMessageRequest, Long senderId) {

        // 1. 발신자 조회
        UserDto sender = getUser(senderId);

        // 2. 채팅방 존재 여부 확인 (선택적)
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

        // 3. 메시지 저장
        ChatMessage message = chatMessageRepository.save(ChatMessage.create(roomId, sender.getId(), sender.getNickname(),
                groupMessageRequest.getContent(), groupMessageRequest.getType()));

        // 4. 그룹 채팅방에 메시지 전송(브로드캐스트)
        broadcastGroupMessage(message);

        // 5. 그룹 채팅방의 다른 참여자들에게 알림 전송
        chatRoom.getParticipants().forEach(participant -> {
            User participantUser = participant.getUser();
            if (!participantUser.getId().equals(senderId)) {
                sendNotification(message, sender, participantUser.getId(), roomId);
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

    // 1:1 메시지 전송
    private void sendDirectMessage(ChatMessage message, Long recipientId) {
        DirectMessageResponse responseDto = message.toDirectMessageDto();
        DirectMessageTransferDto transferDto = new DirectMessageTransferDto(recipientId, responseDto);
        redisPublisher.publish(chatTopic, transferDto);
    }

    // 그룹 메시지 전송
    private void broadcastGroupMessage(ChatMessage message) {
        redisPublisher.publish(chatTopic, message.toGroupMessageDto());
    }

    // 알림 전송
    private void sendNotification(ChatMessage message, UserDto sender, Long recipientId, String roomId) {
        NotificationResponse notification = NotificationResponse.create(sender.getEmail(), message.getContent(),
                sender.getId(), sender.getNickname(), roomId, message.getType());
        NotificationTransferDto notificationTransferDto = new NotificationTransferDto(recipientId, notification);
        redisPublisher.publish(notificationTopic, notificationTransferDto);
    }


}

