package com.goteego.chat.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.GroupMessageResponse;
import com.goteego.chat.dto.message.NotificationResponse;
import com.goteego.chat.dto.message.transfer.DirectMessageTransferDto;
import com.goteego.chat.dto.message.transfer.NotificationTransferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String DIRECT_MESSAGE_PATH = "/queue/messages";
    private static final String GROUP_MESSAGE_PATH = "/sub/chat/room/";
    private static final String NOTIFICATION_PATH = "/queue/notifications";

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 Redis Subscriber가 해당 메시지를 받아 처리
     * ㄴ 직접적으로 호출하는 부분이 없어서 표기상으로 사용되지 않는것처럼 보이지만, RedisConfig에 정의한 MessageListenerAdapter에 의해 호출됨
     */
    public void sendMessage(String publishMessage) {
        try {
            Object messageDto = parseMessage(publishMessage);
            dispatchMessage(messageDto);
        } catch (Exception e) {
            log.error("RedisSubscriber - Failed to process message: {}", publishMessage, e);
        }
    }

    private Object parseMessage(String jsonMessage) throws JsonProcessingException {
        try {
            return objectMapper.readValue(jsonMessage, NotificationTransferDto.class);
        } catch (Exception ignored) {}

        try {
            return objectMapper.readValue(jsonMessage, DirectMessageTransferDto.class);
        } catch (Exception ignored) {}

        try {
            return objectMapper.readValue(jsonMessage, GroupMessageResponse.class);
        } catch (Exception ignored) {}

        throw new IllegalArgumentException("지원되지 않은 메시지 포맷입니다. 현재 값은 {} 입니다." + jsonMessage);
    }

    private void dispatchMessage(Object messageDto) {
        if (messageDto instanceof GroupMessageResponse groupMessage) {
            handleGroupMessage(groupMessage);
        } else if (messageDto instanceof DirectMessageTransferDto directMessageDto) {
            handleDirectMessage(directMessageDto);
        } else if (messageDto instanceof NotificationTransferDto notificationDto) {
            handleNotification(notificationDto);
        } else {
            log.warn("존재하지 않는 메시지 타입입니다. 현재 값은 {} 입니다.", messageDto.getClass().getSimpleName());
        }
    }

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
        NotificationResponse notification = dto.getNotificationResponse();
        sendToUser(dto.getRecipientId(), NOTIFICATION_PATH, notification);
        log.info("RedisSubscriber - Notification sent to user {}", dto.getRecipientId());
    }

    private void sendToUser(Long userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
    }
}