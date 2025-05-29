package com.travelapp.backend.domain.tripday.exception;

import com.travelapp.backend.global.exception.EntityNotFoundException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripDayNotFoundException extends EntityNotFoundException {

    public TripDayNotFoundException() {
        super(ErrorCode.TRIP_DAY_NOT_FOUND);
    }

    public TripDayNotFoundException(Long tripDayId) {
        super(ErrorCode.TRIP_DAY_NOT_FOUND, "여행일을 찾을 수 없습니다. ID: " + tripDayId);
    }

}
