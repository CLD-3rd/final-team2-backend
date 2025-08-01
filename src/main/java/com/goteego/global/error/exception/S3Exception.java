package com.goteego.global.error.exception;

public class S3Exception extends BusinessException {

    public S3Exception(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }

    public S3Exception(ErrorCode errorCode) {
        super(errorCode);
    }
}
