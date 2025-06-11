package com.travelapp.backend.domain.auth.exception;

import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

}
