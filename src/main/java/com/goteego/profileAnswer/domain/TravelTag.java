package com.goteego.profileAnswer.domain;

import lombok.Getter;

import java.util.function.Function;

@Getter
public enum TravelTag {

    IS_ALCHOL3("isAlchol3", "🍶 술 좋아해요", ProfileAnswer::getIsAlchol3),
    IS_ALCHOL2("isAlchol2", "🍻 분위기상 한두 잔 정도", ProfileAnswer::getIsAlchol2),
    IS_ALCHOL1("isAlchol1", "🚫 술은 즐기지 않아요", ProfileAnswer::getIsAlchol1),
    IS_SMOKER("isSmoker", "🚬 흡연 해요", ProfileAnswer::getIsSmoker),

    IS_FRIENDLY("isFriendly", "🤝 새로운 사람과도 금방 친해져요", ProfileAnswer::getIsFriendly),
    IS_QUIET("isQuiet", "🤫 조용한 분위기를 좋아해요", ProfileAnswer::getIsQuiet),
    IS_LEAD("isLead", "🧭 앞장서서 리드하는 편이에요", ProfileAnswer::getIsLead),
    IS_PARTY("isParty", "😎 분위기를 띄우는 걸 좋아해요", ProfileAnswer::getIsParty),
    IS_SEARCH("isSearch", "🤓 여행 중에도 정보를 꼼꼼히 찾는 편이에요", ProfileAnswer::getIsSearch),
    IS_LISTEN("isListen", "👂 다른 사람 의견을 잘 들어주는 편이에요", ProfileAnswer::getIsListen),

    IS_SEE("isSee", "🏞 자연 경관 감상", ProfileAnswer::getIsSee),
    IS_CAFE("isCafe", "🧘 카페/휴식", ProfileAnswer::getIsCafe),
    IS_TASTE("isTaste", "🍽 맛집 탐방", ProfileAnswer::getIsTaste),
    IS_PICTURE("isPicture", "📸 사진 촬영", ProfileAnswer::getIsPicture),
    IS_SHOPPING("isShopping", "🛍 쇼핑", ProfileAnswer::getIsShopping),
    IS_OUTDOOR("isOutdoor", "🏃 액티비티(서핑 등산 등)", ProfileAnswer::getIsOutdoor),

    IS_CHILL("isChill", "💤 느긋하게 여유롭게", ProfileAnswer::getIsChill),
    IS_BUSY("isBusy", "🕘 빡빡하고 알차게", ProfileAnswer::getIsBusy),
    IS_FLEX("isFlex", "❔ 상황에 따라 유동적으로", ProfileAnswer::getIsFlex),

    IS_CITY("isCity", "🌆 도시/핫플 위주", ProfileAnswer::getIsCity),
    IS_HEAL("isHeal", "🏞 자연/힐링 위주", ProfileAnswer::getIsHeal),
    IS_BEACH("isBeach", "🏖 바다/해변", ProfileAnswer::getIsBeach),
    IS_MOUNTAIN("isMountain", "🗻 산/등산", ProfileAnswer::getIsMountain);

    private final String key;
    private final String description;
    private final Function<ProfileAnswer, Boolean> getter;

    TravelTag(String key, String description, Function<ProfileAnswer, Boolean> getter) {
        this.key = key;
        this.description = description;
        this.getter = getter;
    }

    public boolean isSelected(ProfileAnswer profileAnswer) {
        return Boolean.TRUE.equals(getter.apply(profileAnswer));
    }
}
