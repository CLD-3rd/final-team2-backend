package com.goteego.chat.domain;


import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomId; // UUID

    private String name;

    @Enumerated(EnumType.STRING)
    private ChatType type; // DIRECT or GROUP

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<UserChatRoom> participants = new ArrayList<>();

    //@OneToMany(mappedBy = "chatRoom")
    //private List<ChatMessage> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    //== 연관관계 편의 메서드 ==//
    public void addParticipant(User user) {
        if (participants.stream().noneMatch(ucr -> ucr.getUser().equals(user))) {
            UserChatRoom userChatRoom = UserChatRoom.of(user, this);
            participants.add(userChatRoom);
            user.getUserChatRooms().add(userChatRoom);
        }
    }

    // 개인 채팅방 생성
    public static ChatRoom createDirectChat() {
        ChatRoom room = new ChatRoom();
        room.roomId = UUID.randomUUID().toString();
        room.type = ChatType.DIRECT;
        room.createdAt = LocalDateTime.now();
        return room;
    }

    // 그룹 채팅방 생성
    public static ChatRoom createGroupChat(String name) {
        ChatRoom room = new ChatRoom();
        room.roomId = UUID.randomUUID().toString();
        room.type = ChatType.GROUP;
        room.name = name;
        room.createdAt = LocalDateTime.now();
        return room;
    }

}
