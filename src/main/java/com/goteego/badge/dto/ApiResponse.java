package com.goteego.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private String message;
    private T content;

    public static <T> ApiResponse<T> of(String message, T content) {
        return ApiResponse.<T>builder()
                .message(message)
                .content(content)
                .build();
    }
}