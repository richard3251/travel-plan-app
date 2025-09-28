package com.travelapp.backend.domain.tripshare.exception;

import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripShareAccessDeniedException extends BusinessException {

    public TripShareAccessDeniedException() {
        super(ErrorCode.TRIP_ACCESS_DENIED);
    }

    public TripShareAccessDeniedException(String reason) {
        super(ErrorCode.TRIP_ACCESS_DENIED, reason);
    }
}
