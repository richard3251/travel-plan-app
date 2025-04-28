package com.travelapp.backend.domain.tripday.dto.request;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class TripDayCreateRequest {

    private LocalDate date;

}
