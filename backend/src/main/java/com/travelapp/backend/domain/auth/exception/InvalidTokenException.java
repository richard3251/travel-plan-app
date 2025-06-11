package com.travelapp.backend.domain.auth.exception;

import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

}
