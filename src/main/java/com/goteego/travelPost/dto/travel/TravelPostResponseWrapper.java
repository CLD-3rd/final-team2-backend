package com.goteego.travelPost.dto.travel;

import com.goteego.global.dto.PageInfo;
import com.goteego.travelPost.domain.enumerate.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PostType별 여행 게시글 응답 래퍼
 * API 응답 시 PostType에 따라 다른 구조의 데이터를 반환
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPostResponseWrapper {

    private PostType postType;
    private List<BeforeTravelPostResponseDto> beforePosts; // BEFORE 타입일 때
    private List<NowTravelPostResponseDto> nowPosts; // NOW 타입일 때
    private PageInfo pageInfo;

    /**
     * BEFORE 타입 응답 생성
     */
    public static TravelPostResponseWrapper before(List<BeforeTravelPostResponseDto> posts, PageInfo pageInfo) {
        return TravelPostResponseWrapper.builder()
                .postType(PostType.BEFORE)
                .beforePosts(posts)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * NOW 타입 응답 생성
     */
    public static TravelPostResponseWrapper now(List<NowTravelPostResponseDto> posts, PageInfo pageInfo) {
        return TravelPostResponseWrapper.builder()
                .postType(PostType.NOW)
                .nowPosts(posts)
                .pageInfo(pageInfo)
                .build();
    }
} 