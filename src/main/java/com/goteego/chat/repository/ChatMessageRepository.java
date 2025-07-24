package com.goteego.chat.repository;


import com.goteego.chat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지를 시간순으로 조회
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);
}