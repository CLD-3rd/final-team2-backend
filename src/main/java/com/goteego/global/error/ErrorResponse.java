package com.goteego.global.error;

import com.goteego.global.error.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private String message;
    private HttpStatus status;
    private int statusCode;
    private String path;
    private List<FieldErrorDetail> errors;

    private ErrorResponse(ErrorCode code, String path, List<FieldErrorDetail> errors) {
        this.message = code.getMessage();
        this.status = code.getStatus();
        this.statusCode = code.getStatus().value();
        this.path = path;
        this.errors = errors;
    }

    public static ErrorResponse from(ErrorCode code, String path) {
        return new ErrorResponse(code, path, null);
    }

    public static ErrorResponse fromValidationErrors(BindingResult bindingResult, String path) {
        List<FieldErrorDetail> details = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null,
                        error.getDefaultMessage()
                ))
                .toList();
        return new ErrorResponse(ErrorCode.INVALID_INPUT_VALUE, path, details);
    }

    public record FieldErrorDetail(String field, String rejectedValue, String reason) {
    }
}