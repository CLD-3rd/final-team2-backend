package com.goteego.badge.controller;

import com.goteego.badge.dto.ApiResponse;
import com.goteego.badge.dto.BadgeListResponse;
import com.goteego.badge.dto.BadgeResponse;
import com.goteego.badge.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/badge")
public class UserBadgeController {

    private final BadgeService badgeService;

    public UserBadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * 사용자 뱃지 목록 조회
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<BadgeListResponse>> getUserBadges(@PathVariable Long userId) {
        List<BadgeResponse> badges = badgeService.getBadgesByUserId(userId);

        if (badges.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        BadgeListResponse response = BadgeListResponse.of(userId, badges);
        return ResponseEntity.ok(ApiResponse.of("뱃지 조회", response));
    }


}
