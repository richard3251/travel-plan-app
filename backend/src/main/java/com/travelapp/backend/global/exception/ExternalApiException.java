package com.travelapp.backend.global.exception;

import com.travelapp.backend.global.exception.dto.ErrorCode;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
