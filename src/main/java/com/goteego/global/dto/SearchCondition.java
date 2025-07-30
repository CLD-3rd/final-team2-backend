package com.goteego.global.dto;

import jakarta.validation.constraints.Pattern;

public record SearchCondition(
        @Pattern(regexp = "^(view|recent)$", message = "sort는 view 또는 recent만 허용됩니다.")
        String sort,
        String title,
        String author,
        String location
) {
    /**
     * 주어진 SearchCondition 객체에서 null이 아닌 제목, 작성자, 지역 값을 검사하여
     * 해당 조건들을 문자열로 반환하는 메서드입니다.
     *
     * @param condition 검색 조건을 담고 있는 SearchCondition 객체
     * @return 조건이 포함된 문자열, 조건이 없다면 null 반환
     */
    public static String getSearchTerms(SearchCondition condition) {
        StringBuilder searchTerms = new StringBuilder();

        if (condition.title != null && !condition.title.isBlank()) {
            searchTerms.append("제목: ").append(condition.title).append(", ");
        }
        if (condition.author != null && !condition.author.isBlank()) {
            searchTerms.append("작성자: ").append(condition.author).append(", ");
        }
        if (condition.location != null && !condition.location.isBlank()) {
            searchTerms.append("지역: ").append(condition.location);
        }

        // 마지막에 추가된 ", "를 제거
        if (searchTerms.length() > 0 && searchTerms.charAt(searchTerms.length() - 2) == ',') {
            searchTerms.setLength(searchTerms.length() - 2);
        }

        // 결과값이 비어있으면 null 반환
        return searchTerms.length() > 0 ? searchTerms.toString() : null;
    }
}
