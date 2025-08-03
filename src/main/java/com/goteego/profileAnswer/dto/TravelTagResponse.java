package com.goteego.profileAnswer.dto;

import com.goteego.profileAnswer.domain.TravelTag;
import lombok.Builder;

@Builder
public record TravelTagResponse(
        String key,
        String description
) {
    public static TravelTagResponse from(TravelTag tag) {
        return TravelTagResponse.builder()
                .key(tag.getKey())
                .description(tag.getDescription())
                .build();
    }
}
