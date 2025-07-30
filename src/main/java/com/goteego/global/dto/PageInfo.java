package com.goteego.global.dto;

public record PageInfo(
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
    public static PageInfo from(org.springframework.data.domain.Page<?> page) {
        return new PageInfo(
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}
