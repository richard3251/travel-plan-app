package com.travelapp.backend.domain.tripplace.service;

import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripday.repository.TripDayRepository;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceCreateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.VisitOrderUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.domain.tripplace.entity.TripPlace;
import com.travelapp.backend.domain.tripplace.repository.TripPlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final TripPlaceRepository tripPlaceRepository;
    private final TripDayRepository tripDayRepository;

    @Transactional
    public TripPlaceResponse createTripPlace(Long tripDayId, TripPlaceCreateRequest request) {

        TripDay tripDay = tripDayRepository.findById(tripDayId).orElseThrow(
            () -> new IllegalArgumentException("해당 여행날짜는 존재하지않습니다.")
        );

        TripPlace tripPlace = TripPlace.builder()
            .tripDay(tripDay)
            .placeName(request.getPlaceName())
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .memo(request.getMemo())
            .placeId(request.getPlaceId())
            .visitTime(request.getVisitTime())
            .visitOrder(request.getVisitOrder())
            .build();

        return TripPlaceResponse.of(tripPlaceRepository.save(tripPlace));
    }

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripDayId) {

        return tripPlaceRepository.findByTripDay_Id(tripDayId).stream()
            .map(TripPlaceResponse :: of)
            .toList();
    }

    @Transactional
    public TripPlaceResponse updateTripPlace(Long placeId, TripPlaceUpdateRequest request) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripPlace.update(request);
        tripPlaceRepository.save(tripPlace);

        return TripPlaceResponse.of(tripPlace);
    }

    @Transactional
    public TripPlaceResponse updateVisitOrder(Long placeId, VisitOrderUpdateRequest request) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripPlace.visitOrderUpdate(request);
        tripPlaceRepository.save(tripPlace);

        return TripPlaceResponse.of(tripPlace);
    }

    @Transactional
    public void delete(Long placeId) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripPlaceRepository.delete(tripPlace);
    }

    private TripPlace existsTripPlace(Long placeId) {

        return tripPlaceRepository.findById(placeId).orElseThrow(
            () -> new IllegalArgumentException("해당 장소는 존재하지않습니다.")
        );
    }



}
