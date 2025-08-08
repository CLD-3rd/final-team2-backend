package com.goteego.chat.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.GroupMessageResponse;
import com.goteego.chat.dto.message.NotificationResponse;
import com.goteego.chat.dto.message.transfer.DirectMessageTransferDto;
import com.goteego.chat.dto.message.transfer.NotificationTransferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelTopic notificationTopic;
    private final ChannelTopic directChatTopic;
    private final ChannelTopic groupChatTopic;

    private static final String DIRECT_MESSAGE_PATH = "/queue/messages";
    private static final String GROUP_MESSAGE_PATH = "/sub/chat/room/";
    private static final String NOTIFICATION_PATH = "/queue/notifications";

    public RedisSubscriber(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate,
                           @Qualifier("directChatTopic") ChannelTopic directChatTopic,
                           @Qualifier("groupChatTopic") ChannelTopic groupChatTopic,
                           @Qualifier("notificationTopic") ChannelTopic notificationTopic) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.directChatTopic = directChatTopic;
        this.groupChatTopic = groupChatTopic;
        this.notificationTopic = notificationTopic;
    }

    public void sendMessage(String publishMessage, String channel) {
        try {
            log.info("✅ Redis에서 메시지 수신, 채널: {}", channel);
            log.warn("이거실행되는데?이거실행되는데?이거실행되는데?이거실행되는데?이거실행되는데?이거실행되는데?이거실행되는데?");

            // 채널 이름으로 메시지 타입 구분
            if (channel.equals(directChatTopic.getTopic())) {
                // 1:1 채팅 메시지 처리
                DirectMessageTransferDto directMessageDto = objectMapper.readValue(publishMessage, DirectMessageTransferDto.class);
                handleDirectMessage(directMessageDto);

            } else if (channel.equals(groupChatTopic.getTopic())) {
                // 그룹 채팅 메시지 처리
                GroupMessageResponse groupMessage = objectMapper.readValue(publishMessage, GroupMessageResponse.class);
                handleGroupMessage(groupMessage);

            } else if (channel.equals(notificationTopic.getTopic())) {
                // 알림 메시지 처리
                NotificationTransferDto notificationDto = objectMapper.readValue(publishMessage, NotificationTransferDto.class);
                handleNotification(notificationDto);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 에러 발생: {}", publishMessage, e);
        }
    }


    // 1. SimpMessagingTemplate은 이 메시지를 자신의 서버에 있는 로컬 SimpleBroker에게 전달
    // 2. 로컬 SimpleBroker는 자신에게 연결된 모든 웹소켓 세션을 모두 조회
    // 3. 세션들 중에서 /sub/chat/room/... 경로를 구독(subscribe)하고 있는 클라이언트에게만 메시지를 전달
    // 4. 만약 이 서버에 해당 채팅방을 구독 중인 클라이언트가 한 명도 없다면, SimpleBroker는 아무 일도 하지 않고 조용히 작업을 종료
    private void handleGroupMessage(GroupMessageResponse message) {
        messagingTemplate.convertAndSend(GROUP_MESSAGE_PATH + message.getRoomId(), message);
        log.info("RedisSubscriber - Group message sent to /sub/chat/room/{}", message.getRoomId());
    }

    private void handleDirectMessage(DirectMessageTransferDto dto) {
        DirectMessageResponse message = dto.getMessageResponse();

        sendToUser(dto.getRecipientId(), DIRECT_MESSAGE_PATH, message);
        sendToUser(message.getSenderId(), DIRECT_MESSAGE_PATH, message);

        log.info("RedisSubscriber - Direct message sent | Recipient: {}, Sender: {}",
                dto.getRecipientId(), message.getSenderId());
    }

    private void handleNotification(NotificationTransferDto dto) {
        NotificationResponse notification = dto.getMessageResponse();
        sendToUser(dto.getRecipientId(), NOTIFICATION_PATH, notification);
        log.info("RedisSubscriber - Notification sent to user {}", dto.getRecipientId());
    }


    // 1. SimpMessagingTemplate은 자신의 서버에 연결된 모든 웹소켓 세션 중에서, StompHandler에서 설정했던 Principal의 이름(name)이 "userId"와 일치하는 세션 조회
    // 2. 일치하는 세션을 찾으면, 해당 클라이언트에게만 메시지를 전달
    // 3. 만약 이 서버에 해당 "userId"를 가진 사용자의 세션이 없다면, 아무에게도 메시지를 보내지 않고 조용히 작업을 종료
    private void sendToUser(Long userId, String destination, Object payload) {
        log.info("Attempting to send to user: {}, destination: {}", userId, destination);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
    }


}