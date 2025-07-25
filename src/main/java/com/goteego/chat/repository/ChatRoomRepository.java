package com.goteego.chat.repository;


import com.goteego.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);

    /**
     * 두 사용자가 모두 참여하고 있는 DIRECT 타입의 채팅방을 찾는 쿼리
     * ㄴ 1:1 채팅방 중복 생성을 방지하기 위함
     */
    @Query("""
    SELECT cr FROM ChatRoom cr
    JOIN UserChatRoom ucr1 ON ucr1.chatRoom = cr AND ucr1.user.id = :user1Id
    JOIN UserChatRoom ucr2 ON ucr2.chatRoom = cr AND ucr2.user.id = :user2Id
    WHERE cr.type = 'DIRECT'
    """)
    Optional<ChatRoom> findDirectChatRoomByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}
