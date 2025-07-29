package com.goteego.badge.domain.enumerate;

public enum BadgeCode {
    LANDMARK_EIFFEL("에펠탑", "에펠탑 여행 기념 뱃지입니다."),
    LANDMARK_HALLA("한라산", "한라산 정상을 오른 당신을 위한 뱃지입니다."),
    LANDMARK_GWANGHWAMUN("광화문", "광화문을 방문한 기념 뱃지입니다."),
    LANDMARK_HAESHA("해운대", "부산 해운대를 방문한 기념 뱃지입니다."),
    LANDMARK_EWORLD("이월드", "대구 이월드를 방문한 기념 뱃지입니다."),
    LANDMARK_EXPOBRIDGE("엑스포다리", "대전 엑스포다리를 방문한 기념 뱃지입니다."),
    LANDMARK_JEONJUHANOK("전주한옥마을", "전주 한옥마을을 방문한 기념 뱃지입니다."),
    LANDMARK_U_SQUARE("유스퀘어", "광주 유스퀘어를 방문한 기념 뱃지입니다."),
    LANDMARK_BULGUKSA("불국사", "경주 불국사를 방문한 기념 뱃지입니다."),
    LANDMARK_ANMOKCAFE("안목카페거리", "강릉 안목카페거리를 방문한 기념 뱃지입니다."),
    LANDMARK_SEORAKSAN("설악산", "속초 설악산을 방문한 기념 뱃지입니다.");


    private final String name;
    private final String description;

    BadgeCode(String name, String description) {
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
