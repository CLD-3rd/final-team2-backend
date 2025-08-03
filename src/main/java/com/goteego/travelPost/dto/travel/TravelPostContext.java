package com.goteego.travelPost.dto.travel;

import com.goteego.travelPost.domain.TravelPost;

public record TravelPostContext(
        TravelPost tp,
        Long currentUserId,
        String nickname,
        Long viewCount) {
}
