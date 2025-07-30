package com.goteego.chat.service;


import com.goteego.chat.domain.ChatMessage;
import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.dto.message.DirectMessageRequest;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.GroupMessageRequest;
import com.goteego.chat.dto.message.NotificationResponse;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.chat.repository.ChatMessageRepository;
import com.goteego.user.repository.UserRepository;
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
    private final UserRepository userRepository;
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
        sendToEachOther(message.toDirectMessageDto(), sender.getId(), recipient.getId());

        // 4. 수신자에게 알림 전송
        NotificationResponse notification = new NotificationResponse(
                sender.getOauthInfo().getOauthEmail(), // 알림 제목
                message.getContent(), // 알림 내용 (메시지 내용)
                sender.getId(),
                sender.getNickname(),
                roomId
        );
        messagingTemplate.convertAndSendToUser(recipient.getId().toString(), NOTIFICATION_PATH, notification);
    }

    // sendDirectMessage의 내부 메서드
    private void sendToEachOther(DirectMessageResponse message, Long senderId, Long recipientId) {
        messagingTemplate.convertAndSendToUser(recipientId.toString(), DIRECT_MESSAGE_PATH, message);
        messagingTemplate.convertAndSendToUser(senderId.toString(), DIRECT_MESSAGE_PATH, message);
    }

    /**
     * 그룹 채팅 메시지 전송
     */
    @Transactional
    public void sendGroupMessage(String roomId, GroupMessageRequest groupMessageRequest, Long senderId) {

        log.info("roomId = {}", roomId);

        // 1. 발신자 조회
        User sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalStateException("USER NOT FOUND"));
        log.info("sender id = {}", sender.getId());

        // 2. 채팅방 존재 여부 확인 (선택적)
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));
        log.info("Group Chat Room UUID id = {}", chatRoom.getRoomId());

        // 3. 메시지 저장
        ChatMessage message = chatMessageRepository.save(
                ChatMessage.create(
                        roomId,
                        sender.getId(),
                        sender.getNickname(),
                        groupMessageRequest.getContent(),
                        groupMessageRequest.getType()
                )
        );

        // 4. 그룹 채팅방에 메시지 전송(브로드캐스트)
        messagingTemplate.convertAndSend(GROUP_MESSAGE_PATH + roomId, message.toGroupMessageDto());

        // [confirm] - 나중에 확장하면 따로 Notification 패키지로 만들어야 함
        // 5. ✅ 그룹 채팅방의 다른 참여자들에게 알림 전송
        log.info("채팅방 참여자 수: {}", chatRoom.getParticipants().size());
        chatRoom.getParticipants().forEach(participant -> {
            User participantUser = participant.getUser();
            log.info("참여자 ID: {}, 이메일: {}", participantUser.getId(), participantUser.getOauthInfo().getOauthEmail());

            if (!participantUser.getId().equals(senderId)) {
                NotificationResponse notification = new NotificationResponse(
                        chatRoom.getName(),
                        message.getContent(),
                        sender.getId(),
                        sender.getNickname(),
                        roomId
                );
                log.info("알림 전송 대상: {}", participantUser.getId());
                messagingTemplate.convertAndSendToUser(
                        participantUser.getId().toString(),
                        NOTIFICATION_PATH,
                        notification
                );
            }
        });
    }


    /**
     * 특정 채팅방의 메시지 내역 조회
     */
//    public List<DirectMessageResponse> findChatMessages(String roomId) {
//        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
//                .stream()
//                .map(DirectMessageResponse::fromEntity)
//                .collect(Collectors.toList());
//    }
    public Slice<DirectMessageResponse> findChatMessages(String roomId, Pageable pageable) {
        Slice<ChatMessage> messageSlice = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        return messageSlice.map(DirectMessageResponse::fromEntity);
    }



}
