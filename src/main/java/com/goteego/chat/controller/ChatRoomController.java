package com.goteego.chat.controller;


import com.goteego.chat.dto.chatroom.DirectChatRoomDto;
import com.goteego.chat.dto.chatroom.GroupChatRoomDto;
import com.goteego.chat.dto.message.DirectMessageResponse;
import com.goteego.chat.service.ChatRoomService;
import com.goteego.chat.service.ChatService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    /**********************************
     *********** 1:1 채팅방*************
     *********************************/

    // 1:1 채팅방 생성 (만약 이미 존재하면 무시)
    @PostMapping("/direct/{otherUserId}")
    public ResponseEntity<?> createDirectChatRoom(@PathVariable Long otherUserId, @AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();
        try {
            DirectChatRoomDto chatRoomDto = chatRoomService.createOrGetDirectChatRoom(currentUserId, otherUserId);
            return ResponseEntity.ok(chatRoomDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내가 속한 1:1 채팅방 조회
    @GetMapping("/my-rooms/direct")
    public ResponseEntity<List<DirectChatRoomDto>> getMyDirectChatRooms(@AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();
        List<DirectChatRoomDto> myChatRooms = chatRoomService.findMyDirectChatRooms(currentUserId);
        return ResponseEntity.ok(myChatRooms);
    }



    /**********************************
     ********** 그룹 채팅방 ************
     *********************************/

    // 내가 속한 그룹 채팅방 조회
    @GetMapping("/my-rooms/group")
    public ResponseEntity<List<GroupChatRoomDto>> getMyGroupChatRooms(@AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();
        List<GroupChatRoomDto> myGroupChatRooms = chatRoomService.findMyGroupChatRooms(currentUserId);
        return ResponseEntity.ok(myGroupChatRooms);
    }



    /**********************************
     ************** 공통 **************
     *********************************/
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Slice<DirectMessageResponse>> loadMessages(
            @PathVariable String roomId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chatService.findChatMessages(roomId, pageable));
    }

}
