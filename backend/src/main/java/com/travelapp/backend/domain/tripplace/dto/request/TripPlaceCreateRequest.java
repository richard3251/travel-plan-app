package com.travelapp.backend.domain.tripplace.dto.request;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class TripPlaceCreateRequest {

    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String memo;
    private String placeId;
    private LocalTime visitTime;
    private Integer visitOrder;

}
