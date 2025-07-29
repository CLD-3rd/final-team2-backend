package com.goteego.badge.domain;

import com.goteego.global.domain.BaseEntity;
import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "user_badges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class UserBadge extends BaseEntity {
    // user_badge_id (PK)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    Badge badge;

}
