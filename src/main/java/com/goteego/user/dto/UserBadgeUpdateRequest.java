package com.goteego.user.dto;

import java.util.List;

public record UserBadgeUpdateRequest(
        List<Long> badgeIds
) {
}
