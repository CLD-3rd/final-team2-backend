package com.goteego.chat.repository;


import com.goteego.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    // 특정 유저가 속한 모든 채팅방 정보 조회
    @Query("SELECT ucr FROM UserChatRoom ucr JOIN FETCH ucr.chatRoom WHERE ucr.user.id = :userId")
    List<UserChatRoom> findByUserId(Long userId);

    // 특정 유저가 특정 채팅방에 참여 중인지 확인
    boolean existsByChatRoom_RoomIdAndUser_Id(String roomId, Long userId);

    @Query("""
        SELECT DISTINCT ucr FROM UserChatRoom ucr
        JOIN FETCH ucr.chatRoom cr
        LEFT JOIN FETCH cr.participants p
        LEFT JOIN FETCH p.user
        WHERE ucr.user.id = :userId
        """)
    List<UserChatRoom> findChatRoomsWithParticipantsByUserId(@Param("userId") Long userId);

}
