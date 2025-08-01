package com.goteego.travelPost.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 페이징 응답 DTO
 * API 응답 시 페이징 정보를 포함한 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDto<T> {
    private List<T> content;
    private PageableDto pageable;
    private Long totalElements;
    private Integer totalPages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageableDto {
        private Integer pageNumber;
        private Integer pageSize;
    }
} 