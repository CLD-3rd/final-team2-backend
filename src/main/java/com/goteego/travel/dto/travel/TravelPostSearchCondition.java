package com.goteego.travel.dto.travel;

import jakarta.validation.constraints.Pattern;

public record TravelPostSearchCondition(
        @Pattern(regexp = "^(view|recent)$", message = "sort는 view 또는 recent만 허용됩니다.")
        String sort,
        String title,
        String author,
        String location
) {
}
