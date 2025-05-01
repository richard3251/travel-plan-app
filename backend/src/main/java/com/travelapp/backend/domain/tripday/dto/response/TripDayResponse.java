package com.travelapp.backend.domain.tripday.dto.response;

import com.travelapp.backend.domain.tripday.entity.TripDay;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public class TripDayResponse {

    private Long id;
    private Integer day;
    private LocalDate date;
    private Long tripId;

    public static TripDayResponse of(TripDay tripDay) {
        return TripDayResponse.builder()
            .id(tripDay.getId())
            .day(tripDay.getDay())
            .date(tripDay.getDate())
            .tripId(tripDay.getTrip().getId())
            .build();
    }

}
