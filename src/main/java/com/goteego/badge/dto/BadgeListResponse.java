package com.goteego.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class BadgeListResponse {
    private Long userId;
    private List<BadgeResponse> badges;
}