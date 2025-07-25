package com.goteego.travel.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 참가 신청 도메인 엔티티
 * 사용자가 여행 게시글에 참가 신청할 때 생성되는 도메인 객체
 * 참가 신청 → 승인/거절 → 최종 참가자 확정의 과정을 관리
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "participation_application")
public class ParticipationApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_application_id")
    private Long id;
    
    @Column(name = "travel_post_id")
    private Long travelPostId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @CreatedDate
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;
    
    /**
     * 참가 신청 상태 열거형
     */
    public enum Status {
        /** 대기 중 - 작성자가 아직 승인/거절하지 않은 상태 */
        PENDING, 
        /** 승인됨 - 작성자가 참가를 승인한 상태 */
        APPROVED, 
        /** 거절됨 - 작성자가 참가를 거절한 상태 */
        REJECTED
    }
    
    @Builder
    public ParticipationApplication(Long travelPostId, Long userId, Status status) {
        this.travelPostId = travelPostId;
        this.userId = userId;
        this.status = status != null ? status : Status.PENDING;
    }
    
    /**
     * 참가 신청 상태 변경
     */
    public void updateStatus(Status newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
    }
    
    /**
     * 승인 상태 확인
     */
    public boolean isApproved() {
        return Status.APPROVED.equals(this.status);
    }
    
    /**
     * 거절 상태 확인
     */
    public boolean isRejected() {
        return Status.REJECTED.equals(this.status);
    }
    
    /**
     * 대기 상태 확인
     */
    public boolean isPending() {
        return Status.PENDING.equals(this.status);
    }
    
    /**
     * 승인 가능한 상태인지 확인
     */
    public boolean canBeApproved() {
        return Status.PENDING.equals(this.status);
    }
    
    /**
     * 거절 가능한 상태인지 확인
     */
    public boolean canBeRejected() {
        return Status.PENDING.equals(this.status);
    }
} 