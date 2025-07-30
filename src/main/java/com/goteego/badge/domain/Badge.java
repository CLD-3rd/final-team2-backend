package com.goteego.badge.domain;

import com.goteego.badge.domain.enumerate.BadgeCategory;
import com.goteego.badge.domain.enumerate.BadgeCode;
import com.goteego.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "badge_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Badge extends BaseEntity {
    // badge_id (PK)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(name = "code")
    @Enumerated(EnumType.STRING)
    BadgeCode code;

    @Column(name = "img_url")
    String imgUrl;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    BadgeCategory category;

}
