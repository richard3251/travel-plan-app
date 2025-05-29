package com.travelapp.backend.domain.trip.exception;

import com.travelapp.backend.global.exception.EntityNotFoundException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripNotFoundException extends EntityNotFoundException {

    public TripNotFoundException() {
        super(ErrorCode.TRIP_NOT_FOUND);
    }

    public TripNotFoundException(Long tripId) {
        super(ErrorCode.TRIP_NOT_FOUND, "여행 계획을 찾을 수 없습니다. ID: " + tripId);
    }

}
