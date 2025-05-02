package com.travelapp.backend.domain.tripplace.dto.response;

import com.travelapp.backend.domain.tripplace.entity.TripPlace;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class TripPlaceResponse {

    private Long id;
    private String placeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String memo;
    private LocalTime visitTime;
    private Integer visitOrder;

    private TripPlaceResponse() {}

    public static TripPlaceResponse of(TripPlace tripPlace) {
        
        return TripPlaceResponse.builder()
            .id(tripPlace.getId())
            .placeId(tripPlace.getPlaceId())
            .placeName(tripPlace.getPlaceName())
            .address(tripPlace.getAddress())
            .latitude(tripPlace.getLatitude())
            .longitude(tripPlace.getLongitude())
            .memo(tripPlace.getMemo())
            .visitTime(tripPlace.getVisitTime())
            .visitOrder(tripPlace.getVisitOrder())
            .build();
    }

}
