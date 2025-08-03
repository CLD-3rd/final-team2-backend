package com.goteego.global.domain.enumerate;

import com.goteego.badge.domain.enumerate.BadgeCode;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.InvalidLocationException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 위치 정보 Enum
 * 여행지 위치를 나타내는 공통 열거형
 * Travel Post와 Feed에서 공통으로 사용
 */
@Getter
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

    private static final String INVALID_LOCATION_MESSAGE = "유효하지 않은 지역명입니다: ";
    private static final Map<String, Location> ENGLISH_NAME_MAP = new HashMap<>();
    private static final Map<String, Location> KOREAN_NAME_MAP = new HashMap<>();


    static {
        for (Location location : values()) {
            ENGLISH_NAME_MAP.put(location.name().toLowerCase(), location);
            KOREAN_NAME_MAP.put(location.koreanName, location);
        }
    }

    private final String koreanName;
    private final BadgeCode badgeCode;

    Location(String koreanName, BadgeCode badgeCode) {
        this.koreanName = koreanName;
        this.badgeCode = badgeCode;
    }

    /**
     * ✅ 한글 이름으로 Location 찾기
     *
     * @param koreanName 한글 이름 (예: "제주")
     */
    public static Location fromKoreanName(String koreanName) {
        if (koreanName == null || koreanName.isBlank()) return null;
        Location location = KOREAN_NAME_MAP.get(koreanName);
        if (location == null) {
            throw new InvalidLocationException(ErrorCode.INVALID_LOCATION);
        }
        return location;
    }

    /**
     * ✅ 영어(Enum name)로 Location 찾기
     *
     * @param englishName Enum name (예: "JEJU")
     */
    public static Location fromEnglishName(String englishName) {
        Location location = ENGLISH_NAME_MAP.get(englishName.toLowerCase());
        if (location == null) {
            throw new InvalidLocationException(ErrorCode.INVALID_LOCATION);
        }
        return location;
    }
}