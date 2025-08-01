package com.goteego.chat.repository;


import com.goteego.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long> {

    // 특정 채팅방의 모든 메시지를 시간 내림차순 조회
    Slice<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);
}