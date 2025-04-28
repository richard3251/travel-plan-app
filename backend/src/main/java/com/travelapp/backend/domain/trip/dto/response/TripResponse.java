package com.travelapp.backend.domain.trip.dto.response;

import com.travelapp.backend.domain.trip.entity.Trip;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TripResponse {

    private Long id;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    public static TripResponse of(Trip trip) {
        return TripResponse.builder()
            .id(trip.getId())
            .title(trip.getTitle())
            .startDate(trip.getStartDate())
            .endDate(trip.getEndDate())
            .build();
    }

}
