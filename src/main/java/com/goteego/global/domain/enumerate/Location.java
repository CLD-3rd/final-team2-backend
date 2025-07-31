package com.goteego.global.domain.enumerate;

import com.goteego.badge.domain.enumerate.BadgeCode;

import java.util.Arrays;

/**
 * 위치 정보 Enum
 * 여행지 위치를 나타내는 공통 열거형
 * Travel Post와 Feed에서 공통으로 사용
 */
public enum Location {
    JEJU("제주", BadgeCode.LANDMARK_HALLA),
    SEOUL("서울", BadgeCode.LANDMARK_GWANGHWAMUN),
    BUSAN("부산", BadgeCode.LANDMARK_HAESHA),
    DAEGU("대구", BadgeCode.LANDMARK_EWORLD),
    DAEJEON("대전", BadgeCode.LANDMARK_EXPOBRIDGE),
    JEONJU("전주", BadgeCode.LANDMARK_JEONJUHANOK),
    GWANGJU("광주", BadgeCode.LANDMARK_U_SQUARE),
    GYEONGJU("경주", BadgeCode.LANDMARK_BULGUKSA),
    GANGNEUNG("강릉", BadgeCode.LANDMARK_ANMOKCAFE),
    SOKCHO("속초", BadgeCode.LANDMARK_SEORAKSAN);

    private final String displayName;
    private final BadgeCode badgeCode;

    Location(String displayName, BadgeCode badgeCode) {
        this.displayName = displayName;
        this.badgeCode = badgeCode;
    }

    public static Location fromDisplayName(String displayName) {
        return Arrays.stream(Location.values())
                .filter(location -> location.getDisplayName().equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 지역명입니다: " + displayName));
    }

    public static Location fromString(String name) {
        return Arrays.stream(Location.values())
                .filter(location -> location.name().equalsIgnoreCase(name))  // 대소문자 구분 없이 비교
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 지역명입니다: " + name));
    }

    public String getDisplayName() {
        return displayName;
    }

    public BadgeCode toBadgeCode() {
        return badgeCode;
    }
} 