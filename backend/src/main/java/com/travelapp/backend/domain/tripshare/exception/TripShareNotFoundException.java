package com.travelapp.backend.domain.tripshare.exception;

import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripShareNotFoundException extends BusinessException {

    public TripShareNotFoundException() {
        super(ErrorCode.TRIP_SHARE_NOT_FOUND);
    }

    public TripShareNotFoundException(String shareToken) {
        super(ErrorCode.TRIP_SHARE_NOT_FOUND, "공유 토큰: " + shareToken);
    }
}
