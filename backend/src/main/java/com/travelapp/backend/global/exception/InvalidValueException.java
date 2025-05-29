package com.travelapp.backend.global.exception;

import com.travelapp.backend.global.exception.dto.ErrorCode;

public class InvalidValueException extends BusinessException{

    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidValueException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
