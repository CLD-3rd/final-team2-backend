package com.goteego.chat.domain;

import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private LocalDateTime lastReadAt;

    private UserChatRoom(User user, ChatRoom chatRoom) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.lastReadAt = LocalDateTime.now();
    }

    public static UserChatRoom createConnection(User user, ChatRoom chatRoom) {
        return new UserChatRoom(user, chatRoom);
    }

    public void updateLastReadAt() {
        this.lastReadAt = LocalDateTime.now();
    }

}
