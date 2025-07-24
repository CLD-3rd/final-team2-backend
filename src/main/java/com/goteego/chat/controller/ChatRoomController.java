package com.goteego.chat.controller;


import com.goteego.chat.dto.ChatMessageDto;
import com.goteego.chat.dto.ChatRoomDto;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.chat.service.ChatService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    /**
     * 1:1 채팅방 생성 (만약 이미 존재하면 무시)
     */
    @PostMapping("/direct/{otherUserId}")
    public ResponseEntity<?> createDirectChatRoom(@PathVariable Long otherUserId, @AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();
        try {
            ChatRoomDto chatRoomDto = chatRoomService.createOrGetDirectChatRoom(currentUserId, otherUserId);
            return ResponseEntity.ok(chatRoomDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * 내가 속한 채팅방 조회
     */
    @GetMapping("/my-rooms")
    public ResponseEntity<?> getMyChatRooms(@AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();
        List<ChatRoomDto> myChatRooms = chatRoomService.findMyChatRooms(currentUserId);
        return ResponseEntity.ok(myChatRooms);
    }
    //================================================================//


    /**
     * 특정 채팅방의 메시지 내역 조회
     * roomId: 채팅방의 UUID
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable String roomId) {
        List<ChatMessageDto> messages = chatService.findChatMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 접근 가능 여부 확인
     */
    @GetMapping("/rooms/{roomId}/check-access")
    public ResponseEntity<Boolean> checkChatRoomAccess(@PathVariable String roomId, @AuthenticationPrincipal User user) {
        Long userId = user.getId();
        return ResponseEntity.ok(chatRoomService.canUserJoinChatRoom(roomId, userId));
    }

}
