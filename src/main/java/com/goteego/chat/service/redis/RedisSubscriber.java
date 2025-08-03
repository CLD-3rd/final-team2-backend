package com.goteego.chat.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.dto.message.GroupMessageResponse;
import com.goteego.chat.dto.message.NotificationResponse;
import com.goteego.chat.dto.message.transfer.DirectMessageTransferDto;
import com.goteego.chat.dto.message.transfer.NotificationTransferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // ChannelTopic을 주입받아 토픽 이름을 비교하는 데 사용합니다.
    private final ChannelTopic chatTopic;
    private final ChannelTopic notificationTopic;

    private static final String DIRECT_MESSAGE_PATH = "/queue/messages";
    private static final String GROUP_MESSAGE_PATH = "/sub/chat/room/";
    private static final String NOTIFICATION_PATH = "/queue/notifications";

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 Redis Subscriber가 해당 메시지를 받아 처리
     * @param publishMessage 직렬화된 메시지 객체
     * @param channel 메시지가 발행된 채널(토픽) 이름
     */
    public void sendMessage(String publishMessage, String channel) {
        try {
            log.info("✅ Redis에서 메시지 수신, 채널: {}", channel);

            // [수정] 채널 이름으로 메시지 타입을 명확하게 구분
            if (channel.equals(chatTopic.getTopic())) {
                // 채팅 토픽에서 온 메시지 처리
                // GroupMessage와 DirectMessage가 같은 토픽을 사용하므로 내부에서 한 번 더 파싱 시도
                try {
                    // 1:1 메시지 먼저 시도
                    DirectMessageTransferDto directMessageDto = objectMapper.readValue(publishMessage, DirectMessageTransferDto.class);
                    handleDirectMessage(directMessageDto);
                } catch (Exception e) {
                    // 그룹 메시지로 다시 시도
                    GroupMessageResponse groupMessage = objectMapper.readValue(publishMessage, GroupMessageResponse.class);
                    handleGroupMessage(groupMessage);
                }

            } else if (channel.equals(notificationTopic.getTopic())) {
                // 알림 토픽에서 온 메시지 처리
                NotificationTransferDto notificationDto = objectMapper.readValue(publishMessage, NotificationTransferDto.class);
                handleNotification(notificationDto);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 에러 발생: {}", publishMessage, e);
        }
    }


    // handle... 메서드들은 그대로 유지합니다.
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

    private void sendToUser(Long userId, String destination, Object payload) {
        log.info("Attempting to send to user: {}, destination: {}", userId, destination);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
    }
}