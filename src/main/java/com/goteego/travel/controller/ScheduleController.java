package com.goteego.travel.controller;

import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.enumerate.ParticipationStatus;
import com.goteego.travel.dto.participation.ParticipantStatusRequest;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.dto.travel.TravelPostResponseDto;
import com.goteego.travel.service.ScheduleService;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    /**
     * 내 일정 조회
     */
    @GetMapping("/mine")
    public ResponseEntity<List<TravelPostResponseDto>> getMySchedule(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();
        List<TravelPostResponseDto> schedules = scheduleService.getMySchedules(currentUserId, page, size);
        
        log.info("내 일정 조회 - userId: {}, page: {}, size: {}, totalCount: {}", 
                currentUserId, page, size, schedules.size());
        
        return ResponseEntity.ok(schedules);
    }



    //=================================================준형==========================================================//
    /**
     * 참가자 상태 변경 (승인/거절)
     * 여행 게시글 작성자가 참가 신청자의 상태를 승인하거나 거절하는 API
     */
    @PutMapping("/{travelPostId}/participants/{userId}")
    public ResponseEntity<ParticipationApplicationResponseDto> updateParticipantStatus(@PathVariable("travelPostId") Long travelPostId,
                                                                            @PathVariable("userId") Long userId,
                                                                            @RequestBody ParticipantStatusRequest requestDto,
                                                                            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();

        ParticipationApplicationResponseDto participationApplicationResponseDto = scheduleService.updateParticipantStatus(travelPostId, userId,
                ParticipationStatus.valueOf(requestDto.getStatus().toUpperCase()), currentUserId);

        log.info("참가자 상태 변경 - travelPostId: {}, participantUserId: {}, status: {}, currentUserId: {}", 
                travelPostId, userId, participationApplicationResponseDto.getStatus(), currentUserId);
        
        return ResponseEntity.ok(participationApplicationResponseDto);
    }
    
    /**
     * 특정 게시글의 참여자 목록 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 참여자 정보 목록
     */
    @GetMapping("/{travelPostId}/participants")
    public ResponseEntity<List<ParticipationApplicationResponseDto>> getParticipants(@PathVariable("travelPostId") Long travelPostId) {

        List<ParticipationApplicationResponseDto> participants = scheduleService.getParticipants(travelPostId);
        log.info("참여자 목록 조회 - travelPostId: {}, participantCount: {}", travelPostId, participants.size());
        
        return ResponseEntity.ok(participants);
    }
    
    /**
     * 특정 게시글의 참가 신청 목록 조회
     */
    @GetMapping("/{travelPostId}/applications")
    public ResponseEntity<List<ParticipationApplicationResponseDto>> getParticipationApplications(
            @PathVariable("travelPostId") Long travelPostId) {
        
        List<ParticipationApplicationResponseDto> applications = scheduleService.getParticipationApplications(travelPostId);
        log.info("참가 신청 목록 조회 - travelPostId: {}, applicationCount: {}", travelPostId, applications.size());
        
        return ResponseEntity.ok(applications);
    }



    //===========================================================================================================//











    //=================================================재신==========================================================//

    /**
     * 특정 게시글의 승인된 참가자 수 조회
     */
    @GetMapping("/{travelPostId}/participants/approved/count")
    public ResponseEntity<Long> getApprovedParticipantCount(@PathVariable("travelPostId") Long travelPostId) {
        Long count = scheduleService.getApprovedParticipantCount(travelPostId);
        log.info("승인된 참가자 수 조회 - travelPostId: {}, count: {}", travelPostId, count);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 특정 게시글의 대기 중인 참가자 수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 대기 중인 참가자 수
     */
    @GetMapping("/{travelPostId}/participants/pending/count")
    public ResponseEntity<Long> getPendingParticipantCount(@PathVariable("travelPostId") Long travelPostId) {
        Long count = scheduleService.getPendingParticipantCount(travelPostId);
        
        log.info("대기 중인 참가자 수 조회 - travelPostId: {}, count: {}", travelPostId, count);
        
        return ResponseEntity.ok(count);
    }

    //=============================================================================================================//

} 