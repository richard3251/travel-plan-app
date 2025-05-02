package com.travelapp.backend.domain.tripplace.dto.request;

import com.travelapp.backend.domain.place.dto.request.PlaceToTripPlaceRequest;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPlaceCreateRequest {

    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String memo;
    private String placeId;
    private LocalTime visitTime;
    private Integer visitOrder;

    public static TripPlaceCreateRequest of(PlaceToTripPlaceRequest request) {
        return TripPlaceCreateRequest.builder()
            .placeName(request.getPlaceName())
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .memo(request.getMemo())
            .placeId(request.getPlaceId())
            .visitTime(request.getVisitTime())
            .visitOrder(request.getVisitOrder())
            .build();
    }

}
