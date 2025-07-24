package com.goteego.chat.dto;


import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ChatRoomDto {
    private String roomId;
    private String name;
    private ChatType type;
    private Long otherUserId;
    private String otherUserEmail;

    private List<UserChatRoomDto> userChatRoomDtos = new ArrayList<>();

    public ChatRoomDto(ChatRoom chatRoom, Long currentUserId) {
        this.roomId = chatRoom.getRoomId();
        this.name = chatRoom.getName(); // 그룹 채팅 이름
        this.type = chatRoom.getType();

        if (chatRoom.getParticipants() != null) {
            this.userChatRoomDtos = chatRoom.getParticipants().stream()
                    .map(ucr -> new UserChatRoomDto(
                            ucr.getUser().getId(),
                            ucr.getUser().getNickname(),
                            ucr.getLastReadAt() // null일 수도 있음
                    ))
                    .collect(Collectors.toList());
        }
        // 상대방 정보 추출 로직 추가
        chatRoom.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .ifPresent(other -> {
                    this.otherUserId = other.getUser().getId();
                    this.otherUserEmail = other.getUser().getOauthInfo().getOauthEmail();
                    this.name = other.getUser().getNickname();
                });
    }


    public ChatRoomDto(String roomId, String name, ChatType type, List<UserChatRoomDto> userChatRoomDtos) {
        this.roomId = roomId;
        this.name = name;
        this.type = type;
        this.userChatRoomDtos = userChatRoomDtos;
    }
}
