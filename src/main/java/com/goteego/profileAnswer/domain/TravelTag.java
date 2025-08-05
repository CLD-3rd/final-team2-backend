package com.goteego.profileAnswer.domain;

import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.InvalidTravelTagException;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
public enum TravelTag {
    // 성향
    IS_FRIENDLY("isFriendly", "🤝 새로운 사람과도 금방 친해져요", ProfileAnswer::getIsFriendly, ProfileAnswer::setIsFriendly),
    IS_QUIET("isQuiet", "🤫 조용한 분위기를 좋아해요", ProfileAnswer::getIsQuiet, ProfileAnswer::setIsQuiet),
    IS_LEAD("isLead", "🧭 앞장서서 리드하는 편이에요", ProfileAnswer::getIsLead, ProfileAnswer::setIsLead),
    IS_PARTY("isParty", "😎 분위기를 띄우는 걸 좋아해요", ProfileAnswer::getIsParty, ProfileAnswer::setIsParty),
    IS_SEARCH("isSearch", "🤓 여행 중에도 정보를 꼼꼼히 찾는 편이에요", ProfileAnswer::getIsSearch, ProfileAnswer::setIsSearch),
    IS_LISTEN("isListen", "👂 다른 사람 의견을 잘 들어주는 편이에요", ProfileAnswer::getIsListen, ProfileAnswer::setIsListen),

    // 활동
    IS_SEE("isSee", "🏞 자연 경관 감상", ProfileAnswer::getIsSee, ProfileAnswer::setIsSee),
    IS_CAFE("isCafe", "🧘 카페/휴식", ProfileAnswer::getIsCafe, ProfileAnswer::setIsCafe),
    IS_TASTE("isTaste", "🍽 맛집 탐방", ProfileAnswer::getIsTaste, ProfileAnswer::setIsTaste),
    IS_PICTURE("isPicture", "📸 사진 촬영", ProfileAnswer::getIsPicture, ProfileAnswer::setIsPicture),
    IS_SHOPPING("isShopping", "🛍 쇼핑", ProfileAnswer::getIsShopping, ProfileAnswer::setIsShopping),
    IS_OUTDOOR("isOutdoor", "🏃 액티비티(서핑 등산 등)", ProfileAnswer::getIsOutdoor, ProfileAnswer::setIsOutdoor),

    // 스타일
    IS_CHILL("isChill", "💤 느긋하게 여유롭게", ProfileAnswer::getIsChill, ProfileAnswer::setIsChill),
    IS_BUSY("isBusy", "🕘 빡빡하고 알차게", ProfileAnswer::getIsBusy, ProfileAnswer::setIsBusy),
    IS_FLEX("isFlex", "❔ 상황에 따라 유동적으로", ProfileAnswer::getIsFlex, ProfileAnswer::setIsFlex),

    // 음주
    IS_ALCHOL3("isAlchol3", "🍶 술 좋아해요", ProfileAnswer::getIsAlchol3, ProfileAnswer::setIsAlchol3),
    IS_ALCHOL2("isAlchol2", "🍻 분위기상 한두 잔 정도", ProfileAnswer::getIsAlchol2, ProfileAnswer::setIsAlchol2),
    IS_ALCHOL1("isAlchol1", "🚫 술은 즐기지 않아요", ProfileAnswer::getIsAlchol1, ProfileAnswer::setIsAlchol1),

    // 흡연
    IS_SMOKER2("isSmoker2", "🚬 흡연 해요", ProfileAnswer::getIsSmoker2, ProfileAnswer::setIsSmoker2),
    IS_SMOKER1("isSmoker1", "🚭 흡연하지 않아요", ProfileAnswer::getIsSmoker1, ProfileAnswer::setIsSmoker1),

    // 취향
    IS_CITY("isCity", "🌆 도시/핫플 위주", ProfileAnswer::getIsCity, ProfileAnswer::setIsCity),
    IS_HEAL("isHeal", "🏞 자연/힐링 위주", ProfileAnswer::getIsHeal, ProfileAnswer::setIsHeal),
    IS_BEACH("isBeach", "🏖 바다/해변", ProfileAnswer::getIsBeach, ProfileAnswer::setIsBeach),
    IS_MOUNTAIN("isMountain", "🗻 산/등산", ProfileAnswer::getIsMountain, ProfileAnswer::setIsMountain);

    private final String key;
    private final String description;
    private final Function<ProfileAnswer, Boolean> getter;
    private final BiConsumer<ProfileAnswer, Boolean> setter;

    TravelTag(String key, String description,
              Function<ProfileAnswer, Boolean> getter,
              BiConsumer<ProfileAnswer, Boolean> setter) {
        this.key = key;
        this.description = description;
        this.getter = getter;
        this.setter = setter;
    }

    public static TravelTag fromKey(String key) {
        return Arrays.stream(values())
                .filter(tag -> tag.key.equals(key))
                .findFirst()
                .orElseThrow(() -> new InvalidTravelTagException(ErrorCode.INVALID_PROFILE_DATA));
    }

    public boolean isSelected(ProfileAnswer profileAnswer) {
        return Boolean.TRUE.equals(getter.apply(profileAnswer));
    }

    public void update(ProfileAnswer profileAnswer, boolean value) {
        setter.accept(profileAnswer, value);
    }
}
