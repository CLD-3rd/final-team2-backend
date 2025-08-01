package com.goteego.chat.dto.chatroom;

import com.goteego.chat.domain.UserChatRoom;
import com.goteego.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantsDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime lastReadAt; // 마지막 읽은 일시

    // 엔티티 변환 메서드
    public static ChatParticipantsDto from(UserChatRoom userChatRoom) {
        User user = userChatRoom.getUser();

        return ChatParticipantsDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImgUrl())
                .lastReadAt(userChatRoom.getLastReadAt())
                .build();
    }

}
