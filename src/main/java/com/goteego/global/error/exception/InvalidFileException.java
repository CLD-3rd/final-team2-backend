package com.goteego.global.error.exception;

public class InvalidFileException extends BusinessException {
    public InvalidFileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidFileException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
