package com.goteego.global.error.exception;

public class InvalidTravelTagException extends BusinessException {
    public InvalidTravelTagException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public InvalidTravelTagException(ErrorCode errorCode) {
        super(errorCode);
    }
}
