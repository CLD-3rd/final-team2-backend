package com.goteego.global.error;

import com.goteego.global.error.exception.ErrorCode;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    @NotNull
    private String message;

    @NotNull
    private HttpStatus status;

    private String path;

    private ErrorResponse(final ErrorCode code, final String path) {
        this.message = code.getMessage();
        this.status = code.getStatus();
        this.path = path;
    }

    private ErrorResponse(final String message, final HttpStatus status, final String path) {
        this.message = message;
        this.status = status;
        this.path = path;
    }

    public static ErrorResponse from(final ErrorCode code, final String path) {
        return new ErrorResponse(code, path);
    }

    public static ErrorResponse from(final String message, final HttpStatus status, final String path) {
        return new ErrorResponse(message, status, path);
    }

    // 기존 메서드 유지 (경로 포함 안 할 때 사용)
    public static ErrorResponse from(final ErrorCode code) {
        return new ErrorResponse(code, null);
    }

    public static ErrorResponse from(final String message, final HttpStatus status) {
        return new ErrorResponse(message, status, null);
    }
}