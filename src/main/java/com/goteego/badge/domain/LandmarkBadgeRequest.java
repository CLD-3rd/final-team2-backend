package com.goteego.badge.domain;

import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.feed.domain.Feed;
import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "landmark_badge_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class LandmarkBadgeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    BadgeStatus status;

    public void reject() {
        this.status = BadgeStatus.REJECTED;
    }

    public void approve() {
        this.status = BadgeStatus.APPROVED;
    }


}
