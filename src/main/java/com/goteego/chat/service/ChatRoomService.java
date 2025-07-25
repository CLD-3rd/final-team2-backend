package com.goteego.chat.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.UserChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.chat.dto.chatroom.DirectChatRoomDto;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.chat.repository.UserChatRoomRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    // 1:1 채팅방 생성 또는 조회
    @Transactional
    public DirectChatRoomDto createOrGetDirectChatRoom(Long currentUserId, Long otherUserId) {

        validateUsers(currentUserId, otherUserId);

        return chatRoomRepository.findDirectChatRoomByUsers(currentUserId, otherUserId)
                .map(room -> toDto(room, currentUserId))
                .orElseGet(() -> createNewDirectChatRoom(currentUserId, otherUserId));
    }

    private void validateUsers(Long currentUserId, Long otherUserId) {
        if (currentUserId == null || currentUserId.equals(otherUserId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
    }

    // Entity → DTO 변환
    private DirectChatRoomDto toDto(ChatRoom room, Long currentUserId) {
        User otherUser = room.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .map(UserChatRoom::getUser)
                .orElseThrow(() -> new IllegalStateException("채팅방에 상대방이 없습니다."));

        return new DirectChatRoomDto(
                room.getRoomId(),
                room.getType(),
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getOauthInfo().getOauthEmail()
        );
    }

    private DirectChatRoomDto createNewDirectChatRoom(Long currentUserId, Long otherUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        User otherUser = userRepository.findById(otherUserId).orElseThrow();

        ChatRoom directRoom = ChatRoom.createDirectRoom();
        directRoom.addParticipant(currentUser);
        directRoom.addParticipant(otherUser);

        ChatRoom savedRoom = chatRoomRepository.save(directRoom);
        log.info("새로운 채팅방 생성 - roomId: {}, 생성자: {}", savedRoom.getRoomId(), currentUser.getNickname());

        return toDto(savedRoom, currentUserId);
    }


    /**
     * 자신이 속한 채팅방 조회
     */
    public List<DirectChatRoomDto> findMyChatRooms(Long currentUserId) {
        return userChatRoomRepository.findChatRoomsWithParticipantsByUserId(currentUserId)
                .stream()
                .map(ucr -> ucr.getChatRoom())
                .filter(room -> room.getType() == ChatType.DIRECT)
                .map(room -> toDto(room, currentUserId)) // toDto 재사용
                .collect(Collectors.toList());
    }

    // 요청 수락이 된 상태인지 확인
    public boolean canUserJoinChatRoom(String roomId, Long userId) {
        return userChatRoomRepository.existsByChatRoom_RoomIdAndUser_Id(roomId, userId);
    }

    //========================================================================//
//
//    @Transactional
//    public void addUserToGroupChat(String roomId, Long userId) {
//
//        // 수락된 사용자인지 검증
//        validateUserCanJoinGroupChat(roomId, userId);
//
//        ChatRoom chatRoom = getChatRoomById(roomId);
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        chatRoom.addParticipant(user);
//
//        // 채팅방에 입장 메시지 보내기
//        ChatMessageDto messageDto = new ChatMessageDto(
//                MessageType.ENTER,
//                roomId,
//                userId,
//                user.getUsername(),
//                user.getUsername() + "님이 그룹에 참여했습니다.",
//                LocalDateTime.now()
//        );
//        saveMessage(messageDto);
//
//        // WebSocket을 통해 새 참여자 정보 브로드캐스트 하는 과정임 (이거 여기서 /topic/public 했는데 API 명세서때 좀 생각해봐야 할 듯? 알람 이름도 정해야해서)
//        messagingTemplate.convertAndSend("/topic/public/" + roomId, messageDto);
//    }
//
//    ========================================================================//
//
//        public void validateUserCanJoinGroupChat(String roomId, Long userId) {
//        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
//
//        // 작성자는 무조건 입장 허용
//        TravelPost travelPost = travelPostRepository.findByChatRoom(chatRoom)
//                .orElseThrow(() -> new IllegalStateException("채팅방에 연결된 게시글이 없습니다."));
//
//        if (travelPost.getAuthor().getId().equals(userId)) {
//            return;
//        }
//
//        boolean isApproved = participationApplicationRepository.existsByTravelPost_ChatRoom_RoomIdAndRequester_IdAndStatus(roomId, userId, ParticipationStatus.APPROVED);
//
//        if (!isApproved) {
//            throw new IllegalStateException("채팅방 입장 권한이 없습니다. 요청이 수락되지 않았습니다.");
//        }
//    }

}
