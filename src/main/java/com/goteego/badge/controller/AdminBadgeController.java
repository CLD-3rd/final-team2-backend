package com.goteego.badge.controller;


import com.goteego.badge.dto.*;

import com.goteego.badge.service.BadgeService;
import com.goteego.feed.domain.Feed;
import com.goteego.feed.repository.FeedRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/badge")
public class AdminBadgeController {

    private final BadgeService badgeService;


    public AdminBadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }


    /**
     * 뱃지 데이터 init
     */
    @PostMapping("/upload-badge")
    public ResponseEntity<String> triggerUpload() {
        badgeService.uploadToS3();
        return ResponseEntity.ok("Badge uploaded to S3 successfully.");
    }


    /**
     * 뱃지 승인 요청 한 목록 조회
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<BadgeRequestsResponse>>> getBadgeRequests() {
        List<BadgeRequestsResponse> responses = badgeService.getBadgesRequests();
        return ResponseEntity.ok(ApiResponse.of("배지 요청 목록", responses));
    }

    /**
     * 랜드마크 뱃지 승인
     *
     * @param
     * @return
     */
    @PostMapping("/approve/{feedId}")
    public ResponseEntity<ApiResponse<BadgeApprovalResponse>> approveFeedBadge(@PathVariable Long feedId) {

        badgeService.processBadgeByApproval(feedId, true);
        return  ResponseEntity.ok(ApiResponse.of("뱃지 승인", new BadgeApprovalResponse("APPROVED")));
    }


    /**
     * 뱃지 승인 수정
     * 가정: 게시글에 대한 신고가 들어온다 -> 관리자가 승인했던 뱃지에대해서 거절처리
     */
    @PostMapping("/reject/{feedId}")
    public ResponseEntity<ApiResponse<BadgeApprovalResponse>> rejectBadgeFromReportedFeed(@PathVariable Long feedId) {

        badgeService.processBadgeByApproval(feedId, false);
        return ResponseEntity.ok(ApiResponse.of("뱃지 거절", new BadgeApprovalResponse("REJECTED")));
    }

}
