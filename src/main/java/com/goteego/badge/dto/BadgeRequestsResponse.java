package com.goteego.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class BadgeRequestsResponse {
    private Long requestId;  // landmarkBadgeRequest id
    private Long userId;     // 요청자(피드 작성자) id
    private Long feedId;
    private String status;   // PENDING, APPROVED, REJECTED 등
}
