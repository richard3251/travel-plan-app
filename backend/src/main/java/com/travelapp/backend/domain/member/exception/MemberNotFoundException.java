package com.travelapp.backend.domain.member.exception;

import com.travelapp.backend.global.exception.EntityNotFoundException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class MemberNotFoundException extends EntityNotFoundException {

    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }

    public MemberNotFoundException(String message) {
        super(ErrorCode.MEMBER_NOT_FOUND, message)  ;
    }

}
