package com.goteego.feed.dto.response;

import com.goteego.global.dto.PageInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record FeedListResponse(
        List<FeedResponse> feeds,
        PageInfo pageInfo
) {
}
