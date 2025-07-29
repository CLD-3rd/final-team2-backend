package com.goteego.badge.domain.enumerate;

public enum BadgeStatus {
    PENDING("승인요청", "뱃지승인요청"),
    APPROVED("승인", "뱃지승인"),
    REJECTED("거절", "뱃지거절");


    private final String name;
    private final String description;

    BadgeStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
