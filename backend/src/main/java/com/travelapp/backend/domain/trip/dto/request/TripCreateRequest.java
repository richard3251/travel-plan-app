package com.travelapp.backend.domain.trip.dto.request;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class TripCreateRequest {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private Double regionLat;
    private Double regionLng;
}
