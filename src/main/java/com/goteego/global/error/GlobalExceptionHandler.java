package com.goteego.global.error;

import com.goteego.global.error.exception.AccessDeniedException;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ 공통 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(final BusinessException exception, HttpServletRequest request) {
        log.warn("[BusinessException] {} - {}", exception.getErrorCode(), exception.getMessage());
        final ErrorCode errorCode = exception.getErrorCode();
        final String path = request.getRequestURI();

        final ErrorResponse response = ErrorResponse.from(errorCode, path);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * ❌ AccessDeniedException 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        log.warn("[AccessDeniedException] {}", exception.getMessage());
        final ErrorCode errorCode = exception.getErrorCode();
        final String path = request.getRequestURI();

        final ErrorResponse response = ErrorResponse.from(errorCode, path);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 🔎 NotFoundException 처리
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        log.warn("[NotFoundException] {}", exception.getMessage());
        final ErrorCode errorCode = exception.getErrorCode();
        final String path = request.getRequestURI();

        final ErrorResponse response = ErrorResponse.from(errorCode, path);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 🧨 예상치 못한 모든 예외 처리
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception exception, HttpServletRequest request) {
        log.error("[Unhandled Exception]", exception);
        final String path = request.getRequestURI();

        final ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, path);
        return new ResponseEntity<>(response, response.getStatus());
    }
}