package com.goteego.travel.service;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    
    /**
     * 내 일정 조회 (작성자이거나 참여자인 게시글)
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 내가 관련된 여행 게시글 목록
     */
    public List<TravelPost> getMySchedules(Long userId, int page, int size) {
        List<TravelPost> allSchedules = travelPostRepository.findMySchedules(userId);
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allSchedules.size());
        
        if (start >= allSchedules.size()) {
            return List.of();
        }
        
        return allSchedules.subList(start, end);
    }
    
    /**
     * 특정 게시글의 참여자 목록 조회 (REJECTED 제외)
     * 
     * @param postId 게시글 ID
     * @return 참여자 정보 목록 [user_id, nickname, status]
     */
    public List<Object[]> getParticipants(Long postId) {
        return travelPostRepository.findParticipantsByPostId(postId);
    }
    
    /**
     * 참가자 상태 변경 (승인/거절)
     * 
     * @param travelPostId 여행 게시글 ID
     * @param participantUserId 참가자 사용자 ID
     * @param newStatus 변경할 상태
     * @param currentUserId 현재 사용자 ID (권한 확인용)
     * @return 참가자 상태 변경 결과
     * @throws RuntimeException 권한 없음, 게시글 없음, 참가 신청 없음 등의 경우
     */
    @Transactional
    public ParticipationApplication updateParticipantStatus(Long travelPostId, Long participantUserId, 
                                                          ParticipationApplication.Status newStatus, Long currentUserId) {
        
        // 1. 권한 확인 - 작성자인지 확인
        Optional<TravelPost> travelPostOpt = travelPostRepository.findById(travelPostId);
        if (travelPostOpt.isEmpty()) {
            throw new RuntimeException("Travel post not found with id: " + travelPostId);
        }
        
        TravelPost travelPost = travelPostOpt.get();
        if (!travelPost.isAuthor(currentUserId)) {
            throw new RuntimeException("Only the author can update participant status");
        }
        
        // 2. 참여 신청 조회
        Optional<ParticipationApplication> applicationOpt = participationApplicationRepository
                .findByTravelPostIdAndUserId(travelPostId, participantUserId);
        if (applicationOpt.isEmpty()) {
            throw new RuntimeException("Participation application not found");
        }
        
        ParticipationApplication application = applicationOpt.get();
        
        // 3. 상태 변경 가능 여부 확인
        if (newStatus == ParticipationApplication.Status.APPROVED && !application.canBeApproved()) {
            throw new RuntimeException("Application cannot be approved in current status: " + application.getStatus());
        }
        if (newStatus == ParticipationApplication.Status.REJECTED && !application.canBeRejected()) {
            throw new RuntimeException("Application cannot be rejected in current status: " + application.getStatus());
        }
        
        // 4. 상태 변경
        application.updateStatus(newStatus);
        participationApplicationRepository.updateStatusByTravelPostIdAndUserId(travelPostId, participantUserId, newStatus);
        
        return application;
    }
    
    /**
     * 특정 게시글의 승인된 참가자 수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 승인된 참가자 수
     */
    public Long getApprovedParticipantCount(Long travelPostId) {
        return participationApplicationRepository.countByTravelPostIdAndStatus(
            travelPostId, ParticipationApplication.Status.APPROVED);
    }
    
    /**
     * 특정 게시글의 대기 중인 참가자 수 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 대기 중인 참가자 수
     */
    public Long getPendingParticipantCount(Long travelPostId) {
        return participationApplicationRepository.countByTravelPostIdAndStatus(
            travelPostId, ParticipationApplication.Status.PENDING);
    }
    
    /**
     * 특정 게시글의 모든 참가 신청 조회
     * 
     * @param travelPostId 여행 게시글 ID
     * @return 참가 신청 목록
     */
    public List<ParticipationApplication> getParticipationApplications(Long travelPostId) {
        return participationApplicationRepository.findByTravelPostId(travelPostId);
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