package com.goteego.global.domain.enumerate;

/**
 * 위치 정보 Enum
 * 여행지 위치를 나타내는 공통 열거형
 * Travel Post와 Feed에서 공통으로 사용
 */
public enum Location {
    JEJU("제주"),
    SEOUL("서울"),
    BUSAN("부산"),
    DAEGU("대구"),
    DAEJEON("대전"),
    JEONJU("전주"),
    GWANGJU("광주"),
    GYEONGJU("경주"),
    GANGNEUNG("강릉"),
    SOKCHO("속초");

    private final String displayName;

    Location(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 