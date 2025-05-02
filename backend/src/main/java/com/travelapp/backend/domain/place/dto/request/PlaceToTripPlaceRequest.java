package com.travelapp.backend.domain.place.dto.request;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceToTripPlaceRequest {

    private Long tripDayId;
    private String placeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String memo;
    private LocalTime visitTime;
    private Integer visitOrder;

}
