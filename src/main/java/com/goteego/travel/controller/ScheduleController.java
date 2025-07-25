package com.goteego.travel.controller;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 일정 관리 컨트롤러
 * 사용자의 여행 일정 관리 및 참가자 관리 관련 HTTP 요청을 처리하는 REST API 컨트롤러
 * 일정 조회, 참가자 상태 관리 등의 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    /**
     * 내 일정 조회
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param authorization 인증 헤더 (Bearer 토큰)
     * @return 내가 관련된 여행 게시글 목록
     */
    @GetMapping("/mine")
    public ResponseEntity<List<TravelPost>> getMySchedule(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        List<TravelPost> schedules = scheduleService.getMySchedules(currentUserId, page, size);
        
        log.info("내 일정 조회 - userId: {}, page: {}, size: {}, totalCount: {}", 
                currentUserId, page, size, schedules.size());
        
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 참가자 상태 변경 (승인/거절)
     * 여행 게시글 작성자가 참가 신청자의 상태를 승인하거나 거절하는 API
     * 
     * @param travelPostId 대상 여행 게시글 ID
     * @param userId 상태를 변경할 참가자 사용자 ID
     * @param requestDto 요청 본문 (변경할 상태 정보)
     * @param authorization 인증 헤더 (Bearer 토큰)
     * @return 참가자 상태 변경 결과
     */
    @PutMapping("/{travelPostId}/participants/{userId}")
    public ResponseEntity<ParticipationApplication> updateParticipantStatus(
            @PathVariable("travelPostId") Long travelPostId,
            @PathVariable("userId") Long userId,
            @RequestBody ParticipantStatusRequest requestDto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Long currentUserId = extractUserIdFromToken(authorization);
        
        ParticipationApplication application = scheduleService.updateParticipantStatus(
            travelPostId, 
            userId, 
            ParticipationApplication.Status.valueOf(requestDto.getStatus().toUpperCase()),
            currentUserId
        );
        
        log.info("참가자 상태 변경 - travelPostId: {}, participantUserId: {}, status: {}, currentUserId: {}", 
                travelPostId, userId, application.getStatus(), currentUserId);
        
        return ResponseEntity.ok(application);
    }
    
    /**
     * 특정 게시글의 참여자 목록 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 참여자 정보 목록
     */
    @GetMapping("/{travelPostId}/participants")
    public ResponseEntity<List<Object[]>> getParticipants(@PathVariable("travelPostId") Long travelPostId) {
        List<Object[]> participants = scheduleService.getParticipants(travelPostId);
        
        log.info("참여자 목록 조회 - travelPostId: {}, participantCount: {}", travelPostId, participants.size());
        
        return ResponseEntity.ok(participants);
    }
    
    /**
     * 특정 게시글의 참가 신청 목록 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 참가 신청 목록
     */
    @GetMapping("/{travelPostId}/applications")
    public ResponseEntity<List<ParticipationApplication>> getParticipationApplications(
            @PathVariable("travelPostId") Long travelPostId) {
        
        List<ParticipationApplication> applications = scheduleService.getParticipationApplications(travelPostId);
        
        log.info("참가 신청 목록 조회 - travelPostId: {}, applicationCount: {}", travelPostId, applications.size());
        
        return ResponseEntity.ok(applications);
    }
    
    /**
     * 특정 게시글의 승인된 참가자 수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 승인된 참가자 수
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
    
    /**
     * 참가자 정보 디버그 조회
     * 개발/테스트 목적으로 특정 여행 게시글의 특정 참가자 정보를 조회하는 API
     * 실제 운영에서는 제거해야 함
     * 
     * @param travelPostId 여행 게시글 ID
     * @param userId 참가자 사용자 ID
     * @return 참가자 정보 또는 에러 메시지
     */
    @GetMapping("/debug/{travelPostId}/participants/{userId}")
    public ResponseEntity<String> debugParticipant(
            @PathVariable("travelPostId") Long travelPostId,
            @PathVariable("userId") Long userId) {
        
        try {
            Optional<ParticipationApplication> applicationOpt = scheduleService.getParticipationApplications(travelPostId)
                    .stream()
                    .filter(app -> app.getUserId().equals(userId))
                    .findFirst();
                    
            if (applicationOpt.isPresent()) {
                ParticipationApplication app = applicationOpt.get();
                String result = "Found: " + app.getStatus() + " for travel_post_id=" + travelPostId + ", user_id=" + userId;
                log.debug("디버그 조회 결과: {}", result);
                return ResponseEntity.ok(result);
            } else {
                String result = "Not found for travel_post_id=" + travelPostId + ", user_id=" + userId;
                log.debug("디버그 조회 결과: {}", result);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            log.error("디버그 조회 중 에러 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
    
    /**
     * 토큰에서 사용자 ID 추출 (Mock 구현)
     * 
     * @param authorization 인증 헤더
     * @return 사용자 ID
     */
    private Long extractUserIdFromToken(String authorization) {
        // TODO: 추후 OAuth2/JWT 구현 시 실제 토큰 파싱 로직으로 대체
        if (authorization == null || authorization.isEmpty()) {
            return 1L; // Mock 사용자 ID
        }
        
        if (authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            // TODO: JWT 토큰에서 사용자 ID 추출
            return 1L; // Mock 사용자 ID
        }
        
        return 1L; // Mock 사용자 ID
    }
    
    /**
     * 참가자 상태 변경 요청 DTO
     */
    public static class ParticipantStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
} 