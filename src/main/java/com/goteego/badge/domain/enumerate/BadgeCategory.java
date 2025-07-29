package com.goteego.badge.domain.enumerate;

public enum BadgeCategory {
    //LEVEL("레벨", "자동 뱃지"),
    LANDMARK("랜드마크", "관리자 승인필요 뱃지");

    private final String name;
    private final String description;

    BadgeCategory(String name, String description) {
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
