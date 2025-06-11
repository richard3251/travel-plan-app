package com.travelapp.backend.domain.trip.exception;

import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripAccessDeniedException extends BusinessException {

    public TripAccessDeniedException() {
        super(ErrorCode.TRIP_ACCESS_DENIED);
    }

    public TripAccessDeniedException(String message) {
        super(ErrorCode.TRIP_ACCESS_DENIED, message);
    }

}
