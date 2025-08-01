package com.goteego.chat.service;

import com.goteego.chat.domain.ChatRoom;
import com.goteego.chat.domain.UserChatRoom;
import com.goteego.chat.domain.enumerate.ChatType;
import com.goteego.chat.dto.chatroom.ChatParticipantsDto;
import com.goteego.chat.dto.chatroom.DirectChatRoomDto;
import com.goteego.chat.dto.chatroom.GroupChatRoomDto;
import com.goteego.chat.repository.ChatRoomRepository;
import com.goteego.chat.repository.UserChatRoomRepository;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.travelPost.repository.ParticipationApplicationRepository;
import com.goteego.user.domain.User;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final ParticipationApplicationRepository participationApplicationRepository;


    /******************************************************************************************
     **************************************** 1:1 채팅방  **************************************
     *****************************************************************************************/

    // 1:1 채팅방 생성 또는 조회
    @Transactional
    public DirectChatRoomDto createOrGetDirectChatRoom(Long currentUserId, Long otherUserId) {

        validateUsers(currentUserId, otherUserId);

        return chatRoomRepository.findDirectChatRoomByUsers(currentUserId, otherUserId)
                .map(room -> toDirectChatRoomDto(room, currentUserId))
                .orElseGet(() -> createNewDirectChatRoom(currentUserId, otherUserId));
    }

    private void validateUsers(Long currentUserId, Long otherUserId) {
        if (currentUserId == null || currentUserId.equals(otherUserId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
    }

    // Entity → DTO 변환
    private DirectChatRoomDto toDirectChatRoomDto(ChatRoom room, Long currentUserId) {
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

        return toDirectChatRoomDto(savedRoom, currentUserId);
    }

    // 자신이 속한 1:1 채팅방 조회
    public List<DirectChatRoomDto> findMyDirectChatRooms(Long currentUserId) {
        return userChatRoomRepository.findChatRoomsWithParticipantsByUserId(currentUserId)
                .stream()
                .map(UserChatRoom::getChatRoom)
                .filter(room -> room.getType() == ChatType.DIRECT)
                .map(room -> toDirectChatRoomDto(room, currentUserId))
                .collect(Collectors.toList());
    }


    /******************************************************************************************
     ***************************************** 그룹 채팅방  ************************************
     *****************************************************************************************/

    // 그룹 채팅방 생성 또는 조회 (여행 게시글 작성 시, 자동 그룹 채팅방 생성)
    @Transactional
    public ChatRoom createGroupChatRoomForTravelPost(User author, String roomName) {

        ChatRoom groupRoom = ChatRoom.createGroupRoom(roomName + "의 여행 채팅방");
        groupRoom.addParticipant(author);

        ChatRoom savedRoom = chatRoomRepository.save(groupRoom);
        log.info("여행 게시글용 그룹 채팅방 생성 - roomId: {}, 생성자: {}", savedRoom.getRoomId(), author.getNickname());

        return savedRoom;
    }

    // Entity → DTO 변환
    private GroupChatRoomDto toGroupDto(ChatRoom room, Long currentUserId) {
        return GroupChatRoomDto.builder()
                .roomId(room.getRoomId())
                .type(room.getType())
                .participants(room.getParticipants().stream()
                        .map(ChatParticipantsDto::from)
                        .collect(Collectors.toList()))
                .groupName(room.getName())
                .unreadCount(0)
                .build();
    }

    // 그룹 채팅방에 사용자 추가 (참여 요청 승인 시 호출)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addUserToGroupChatRoom(String roomId, Long userId) {

        // 수락된 사용자인지 검증
        if (userChatRoomRepository.existsByChatRoom_RoomIdAndUser_Id(roomId, userId)) {
            return; // 이미 참여 중이면 무시
        }

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        chatRoom.addParticipant(user);
    }

    // 자신이 속한 그룹 채팅방 조회
    public List<GroupChatRoomDto> findMyGroupChatRooms(Long currentUserId) {
        return userChatRoomRepository.findChatRoomsWithParticipantsByUserId(currentUserId)
                .stream()
                .map(UserChatRoom::getChatRoom)
                .filter(room -> room.getType() == ChatType.GROUP)
                .map(room -> toGroupDto(room, currentUserId))
                .collect(Collectors.toList());
    }

}
