package com.goteego.travel.service;

import com.goteego.chat.service.ChatRoomService;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.dto.travel.TravelPostResponseDto;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 일정 관리 서비스
 * 사용자의 여행 일정 관리 및 참가자 관리 로직을 담당하는 서비스 클래스
 * 일정 조회, 참가자 상태 관리 등의 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    
    private final TravelPostRepository travelPostRepository;
    private final ParticipationApplicationRepository participationApplicationRepository;
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    
    /**
     * 내 일정 조회 (작성자이거나 참여자인 게시글)
     */
    public List<TravelPostResponseDto> getMySchedules(Long userId, int page, int size) {
        List<TravelPost> allSchedules = travelPostRepository.findMySchedules(userId);
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allSchedules.size());
        
        if (start >= allSchedules.size()) {
            return List.of();
        }

        List<TravelPost> pagedSchedules = allSchedules.subList(start, end);

        return pagedSchedules.stream()
                .map(tp -> TravelPostResponseDto.from(
                        tp,
                        tp.getUser().getId(),
                        tp.getUser().getNickname(),
                        null // similarity는 이 컨텍스트에선 필요 없다면 null
                ))
                .collect(Collectors.toList());
    }




    //======================================================준형===================================================//

    
    /**
     * 참가자 상태 변경 (승인/거절) -> 승인 시, 게시글 채팅방에 초대
     */
    @Transactional
    public ParticipationApplicationResponseDto updateParticipantStatus(Long travelPostId, Long participantUserId,
                                                            ParticipationStatus newStatus, Long currentUserId) {

        User participant = userService.getUserById(participantUserId);

        // 1. 권한 확인 - 작성자인지 확인
        TravelPost travelPost = travelPostRepository.findById(travelPostId).orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        if (!travelPost.isAuthor(currentUserId)) {
            throw new RuntimeException("오직 게시글 작성자만 참가 요청을 처리할 수 있습니다.");
        }
        
        // 2. 참여 신청 조회
        ParticipationApplication participationApplication = participationApplicationRepository
                                                            .findByTravelPostIdAndUserId(travelPostId, participantUserId).orElseThrow(() ->
                                                            new NotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        
        // 3. 상태 변경 가능 여부 확인
        if (newStatus == ParticipationStatus.APPROVED && !participationApplication.canBeApproved()) {
            throw new RuntimeException("Application cannot be approved in current status: " + participationApplication.getStatus());
        }
        if (newStatus == ParticipationStatus.REJECTED && !participationApplication.canBeRejected()) {
            throw new RuntimeException("Application cannot be rejected in current status: " + participationApplication.getStatus());
        }
        
        // 4. 상태 변경
        participationApplication.updateStatus(newStatus);
        participationApplicationRepository.flush(); // 명시적 flush

        // 5. 승인된 경우 채팅방에 추가
        if (newStatus == ParticipationStatus.APPROVED) {
            try {
                chatRoomService.addUserToGroupChatRoom(
                        travelPost.getChatRoom().getRoomId(),
                        participantUserId
                );
                log.info("참가자 {}를 채팅방 {}에 추가했습니다.", participantUserId, travelPost.getChatRoom().getRoomId());
            } catch (Exception e) {
                log.error("채팅방 추가 실패 - roomId: {}, userId: {}",
                        travelPost.getChatRoom().getRoomId(), participantUserId, e);
            }
        }

        // 6. DTO 변환
        return ParticipationApplicationResponseDto.from(participationApplication, participant);
    }


    /**
     * 특정 게시글의 참여자 목록 조회 (REJECTED 제외)
     */
    public List<ParticipationApplicationResponseDto> getParticipants(Long postId) {

        List<ParticipationApplication> participationApplicationList = travelPostRepository.findNonRejectedByPostId(postId);

        return participationApplicationList.stream()
                .map(application -> ParticipationApplicationResponseDto.from(application, application.getUser()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시글의 모든 참가 신청 조회
     */
    public List<ParticipationApplicationResponseDto> getParticipationApplications(Long travelPostId) {

        List<ParticipationApplication> participationApplicationList = participationApplicationRepository.findByTravelPostId(travelPostId);

        return participationApplicationList.stream()
                .map(application -> ParticipationApplicationResponseDto.from(application, application.getUser()))
                .collect(Collectors.toList());
    }

    //===========================================================================================================//








    //====================================================재신=====================================================//


    
    /**
     * 특정 게시글의 대기 중인 참가자 수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 대기 중인 참가자 수
     */
    public Long getPendingParticipantCount(Long travelPostId) {
        return participationApplicationRepository.countByTravelPostIdAndStatus(
            travelPostId, ParticipationStatus.PENDING);
    }

    /**
     * 특정 게시글의 승인된 참가자 수 조회
     *
     * @param travelPostId 여행 게시글 ID
     * @return 승인된 참가자 수
     */
    public Long getApprovedParticipantCount(Long travelPostId) {
        return participationApplicationRepository.countByTravelPostIdAndStatus(travelPostId, ParticipationStatus.APPROVED);
    }

    
    /**
     * 특정 게시글의 승인된 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 승인된 참가 신청 목록
     */
    public List<ParticipationApplication> getApprovedApplications(Long travelPostId) {
        return participationApplicationRepository.findApprovedByTravelPostId(travelPostId);
    }
    
    /**
     * 특정 게시글의 대기 중인 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 대기 중인 참가 신청 목록
     */
    public List<ParticipationApplication> getPendingApplications(Long travelPostId) {
        return participationApplicationRepository.findPendingByTravelPostId(travelPostId);
    }
    
    /**
     * 진행 상태 계산
     * 
     * @param startTime 시작일
     * @param endTime 종료일
     * @return 진행 상태 (UPCOMING/ONGOING/COMPLETED)
     */
    public String calculateProgressStatus(LocalDate startTime, LocalDate endTime) {
        LocalDate now = LocalDate.now();
        
        if (now.isBefore(startTime)) {
            return "UPCOMING";
        } else if (now.isAfter(endTime)) {
            return "COMPLETED";
        } else {
            return "ONGOING";
        }
    }
    
    /**
     * 사용자가 특정 게시글의 작성자인지 확인
     * 
     * @param travelPostId 게시글 ID
     * @param userId 사용자 ID
     * @return 작성자인지 여부
     */
    public boolean isAuthor(Long travelPostId, Long userId) {
        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(travelPostId);
        return travelPostOpt.isPresent() && travelPostOpt.get().isAuthor(userId);
    }
    
    /**
     * 사용자가 특정 게시글에 참가 신청했는지 확인
     * 
     * @param travelPostId 게시글 ID
     * @param userId 사용자 ID
     * @return 참가 신청 여부
     */
    public boolean hasParticipationApplication(Long travelPostId, Long userId) {
        Optional<ParticipationApplication> applicationOpt = participationApplicationRepository
                .findByTravelPostIdAndUserId(travelPostId, userId);
        return applicationOpt.isPresent();
    }
} 