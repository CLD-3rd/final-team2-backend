package com.goteego.feed.domain.enumerate;

/**
 * 피드 정렬 타입 Enum
 * 피드 목록 조회 시 정렬 기준을 나타내는 열거형
 */
public enum FeedSortType {
    RECENT("recent", "최근순"),
    VIEW("view", "조회순"),
    LIKE("like", "좋아요순");

    private final String value;
    private final String displayName;

    FeedSortType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FeedSortType fromValue(String value) {
        for (FeedSortType sortType : values()) {
            if (sortType.value.equals(value)) {
                return sortType;
            }
        }
        return RECENT; // 기본값
    }
} 