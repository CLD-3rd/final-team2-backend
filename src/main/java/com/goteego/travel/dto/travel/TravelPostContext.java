package com.goteego.travel.dto.travel;

import com.goteego.travel.domain.TravelPost;

public record TravelPostContext(
        TravelPost tp,
        Long currentUserId,
        String nickname) {
}
