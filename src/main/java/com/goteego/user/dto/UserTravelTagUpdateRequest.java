package com.goteego.user.dto;

import java.util.List;

public record UserTravelTagUpdateRequest(
        List<String> travelTagKeys
) {
}
