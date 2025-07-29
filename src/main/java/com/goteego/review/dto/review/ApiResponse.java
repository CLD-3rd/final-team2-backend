package com.goteego.review.dto.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //null인 필드는 JSON에 포함시키지 않음
public class ApiResponse<T> {
    private String message;
    private T content;

    public ApiResponse(String message) {
        this.message = message;
    }

    public static <T> ApiResponse<T> of(String message, T content) {
        return new ApiResponse<>(message, content);
    }

    public static <T> ApiResponse<T> of(String message) {
        return new ApiResponse<>(message);
    }
}