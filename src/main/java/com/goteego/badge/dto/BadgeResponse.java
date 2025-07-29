package com.goteego.badge.dto;

import com.goteego.badge.domain.Badge;
import com.goteego.badge.domain.enumerate.BadgeCategory;
import com.goteego.badge.domain.enumerate.BadgeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BadgeResponse {

    private Long id;
    private BadgeCode code;
    private String imgUrl;
    private BadgeCategory category;
    private String name;
    private String description;

    public static BadgeResponse from(Badge badgeType) {
        return BadgeResponse.builder()
                .id(badgeType.getId())
                .code(badgeType.getCode())
                .imgUrl(badgeType.getImgUrl())
                .category(badgeType.getCategory())
                .name(badgeType.getCode().getName())
                .description(badgeType.getCode().getDescription())
                .build();
    }

}

