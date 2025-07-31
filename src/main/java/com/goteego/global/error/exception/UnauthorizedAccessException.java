package com.goteego.global.error.exception;

public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedAccessException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
