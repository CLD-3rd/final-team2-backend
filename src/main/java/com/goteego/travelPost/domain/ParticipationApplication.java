package com.goteego.travelPost.domain;

import com.goteego.travelPost.domain.enumerate.ParticipationStatus;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static com.goteego.travelPost.domain.enumerate.ParticipationStatus.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "participation_application")
public class ParticipationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_post_id")
    private TravelPost travelPost;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status = PENDING;

    @CreatedDate
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;


    @Builder
    public ParticipationApplication(TravelPost travelPost, User user, ParticipationStatus status) {
        this.travelPost = travelPost;
        this.user = user;
        this.status = status != null ? status : PENDING;
    }


    /**
     * 비즈니스 로직
     */

    // 참가 신청 상태 변경
    public void updateStatus(ParticipationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
    }


    // 승인 상태 확인
    public boolean isApproved() {
        return APPROVED.equals(this.status);
    }

    // 거절 상태 확인
    public boolean isRejected() {
        return REJECTED.equals(this.status);
    }

    // 대기 상태 확인
    public boolean isPending() {
        return PENDING.equals(this.status);
    }

    // 승인 가능한 상태인지 확인
    public boolean canBeApproved() {
        return PENDING.equals(this.status);
    }

    // 거절 가능한 상태인지 확인
    public boolean canBeRejected() {
        return PENDING.equals(this.status);
    }
} 