package com.goteego.global.error.exception;

public class InvalidLocationException extends BusinessException {
    public InvalidLocationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public InvalidLocationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
