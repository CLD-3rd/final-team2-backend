package com.goteego.travel.domain.enumerate;

public enum ParticipationStatus {
    PENDING,        // 작성자가 아직 승인/거절하지 않은 상태
    APPROVED,       // 작성자가 참가를 승인한 상태
    REJECTED        // 작성자가 참가를 거절한 상태
}