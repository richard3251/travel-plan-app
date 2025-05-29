package com.travelapp.backend.domain.tripplace.exception;

import com.travelapp.backend.global.exception.EntityNotFoundException;
import com.travelapp.backend.global.exception.dto.ErrorCode;

public class TripPlaceNotFoundException extends EntityNotFoundException {

    public TripPlaceNotFoundException() {
        super(ErrorCode.TRIP_PLACE_NOT_FOUND);
    }

    public TripPlaceNotFoundException(Long tripPlaceId) {
        super(ErrorCode.TRIP_PLACE_NOT_FOUND, "여행 장소를 찾을 수 없습니다. ID: " + tripPlaceId);
    }
}
