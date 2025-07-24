package com.goteego.chat.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.UserChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.chat.dto.ChatRoomDto;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    /**
     * 1:1 채팅방 생성 
     */
    @Transactional
    public ChatRoomDto createOrGetDirectChatRoom(Long currentUserId, Long otherUserId) {

        if (currentUserId == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(otherUserId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        return chatRoomRepository.findDirectChatRoomByUsers(currentUserId, otherUserId)
                .map(room -> {
                    log.info("기존 채팅방 조회 - roomId: {}", room.getRoomId());
                    return createChatRoomDto(room, currentUserId);
                })
                .orElseGet(() -> createNewDirectChatRoom(currentUserId, otherUserId));
    }

    private ChatRoomDto createNewDirectChatRoom(Long currentUserId, Long otherUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        User otherUser = userRepository.findById(otherUserId).orElseThrow();

        ChatRoom newRoom = ChatRoom.createDirectChat();
        newRoom.addParticipant(currentUser);
        newRoom.addParticipant(otherUser);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        log.info("새로운 채팅방 생성 - roomId: {}, 생성자: {}", savedRoom.getRoomId(), currentUser.getNickname());

        return createChatRoomDto(savedRoom, currentUserId);
    }

    private ChatRoomDto createChatRoomDto(ChatRoom room, Long currentUserId) {
        return room.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .map(other -> new ChatRoomDto(room, currentUserId))
                .orElseGet(() -> new ChatRoomDto(room, currentUserId));
    }


    /**
     * 자신이 속한 채팅방 조회
     */
    public List<ChatRoomDto> findMyChatRooms(Long currentUserId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(currentUserId);

        return userChatRooms.stream()
                .map(ucr -> {
                    ChatRoom room = ucr.getChatRoom();
                    ChatRoomDto dto = new ChatRoomDto(room, currentUserId);

                    // 1:1 채팅이면 상대방 이름을 채팅방 이름으로 설정
                    if (room.getType() == ChatType.DIRECT) {
                        room.getParticipants().stream()
                                .filter(participant -> !participant.getUser().getId().equals(currentUserId))
                                .findFirst()
                                .ifPresent(otherParticipant -> dto.setName(otherParticipant.getUser().getNickname()));
                    }
                    return dto;
                }).collect(Collectors.toList());
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
