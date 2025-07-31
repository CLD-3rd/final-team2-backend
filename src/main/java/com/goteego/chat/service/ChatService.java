package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.MessageType;
import com.goteego.chat.dto.message.DirectMessageRequest;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.GroupMessageRequest;
import com.goteego.chat.dto.message.NotificationResponse;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.user.dto.UserDto;
import com.goteego.user.repository.UserRepository;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    private final String DIRECT_MESSAGE_PATH = "/queue/messages";
    private final String GROUP_MESSAGE_PATH = "/sub/chat/room/";
    private final String NOTIFICATION_PATH = "/queue/notifications";


    /**
     * 1:1 채팅 메시지 전송
     */
    @Transactional
    public void sendDirectMessage(String roomId, DirectMessageRequest directMessageRequest, Long senderId) {
        // 1. 발신자 조회
        UserDto sender = userService.getUser(senderId);

        // 2. 수신자 조회
        UserDto recipient = userService.getUser(directMessageRequest.getRecipientId());

        log.info("sender id = {}", sender.getId());
        log.info("recipient id = {}", recipient.getId());

        // 3. 메시지 DB 저장
        ChatMessage message = createAndSaveMessage(roomId, directMessageRequest.getContent(), directMessageRequest.getType(), sender);

        // 4. 메시지 전송 (수신자와 발신자 모두에게 전송)
        messagingTemplate.convertAndSendToUser(String.valueOf(recipient.getId()), DIRECT_MESSAGE_PATH, message); // 수신자에게
        messagingTemplate.convertAndSendToUser(String.valueOf(sender.getId()), DIRECT_MESSAGE_PATH, message);    // 발신자에게

        // 5. 알림 메시지 임시 저장
        NotificationResponse notification= NotificationResponse.create(sender.getEmail(), message.getContent(), sender.getId(), sender.getNickname(), roomId);

        // 6. 수신자에게 알림 전송
        messagingTemplate.convertAndSendToUser(recipient.getId().toString(), NOTIFICATION_PATH, notification);
    }

    /**
     * 그룹 채팅 메시지 전송
     */
    @Transactional
    public void sendGroupMessage(String roomId, GroupMessageRequest groupMessageRequest, Long senderId) {

        log.info("roomId = {}", roomId);

        // 1. 발신자 조회
        UserDto sender = userService.getUser(senderId);

        // 2. 채팅방 존재 여부 확인 (선택적)
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

        // 3. 메시지 저장
        ChatMessage message = chatMessageRepository.save(ChatMessage.create(roomId, sender.getId(), sender.getNickname(),
                                                            groupMessageRequest.getContent(),groupMessageRequest.getType()));

        // 4. 그룹 채팅방에 메시지 전송(브로드캐스트)
        messagingTemplate.convertAndSend(GROUP_MESSAGE_PATH + roomId, message.toGroupMessageDto());

        // [confirm] - 나중에 확장하면 따로 Notification 패키지로 만들어야 함
        // 5. ✅ 그룹 채팅방의 다른 참여자들에게 알림 전송
        log.info("채팅방 참여자 수: {}", chatRoom.getParticipants().size());
        chatRoom.getParticipants().forEach(participant -> {
            User participantUser = participant.getUser();
            log.info("참여자 ID: {}, 이메일: {}", participantUser.getId(), participantUser.getOauthInfo().getOauthEmail());

            if (!participantUser.getId().equals(senderId)) {
                NotificationResponse notification= NotificationResponse.create(chatRoom.getName(), message.getContent(), sender.getId(), sender.getNickname(), roomId);
                log.info("알림 전송 대상: {}", participantUser.getId());
                messagingTemplate.convertAndSendToUser(participantUser.getId().toString(), NOTIFICATION_PATH, notification);
            }
        });
    }


    /**
     * 특정 채팅방의 메시지 내역 조회
     */
    public Slice<DirectMessageResponse> findChatMessages(String roomId, Pageable pageable) {
        Slice<ChatMessage> messageSlice = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        return messageSlice.map(DirectMessageResponse::fromEntity);
    }


    //===============================내부로직===============================//
    private ChatMessage createAndSaveMessage(String roomId, String content, MessageType type, UserDto sender) {
        return chatMessageRepository.save(ChatMessage.create(roomId, sender.getId(), sender.getNickname(), content, type));
    }

}
