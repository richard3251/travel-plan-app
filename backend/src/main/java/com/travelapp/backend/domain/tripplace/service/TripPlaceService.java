package com.travelapp.backend.domain.tripplace.service;

import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripday.exception.TripDayNotFoundException;
import com.travelapp.backend.domain.tripday.repository.TripDayRepository;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceCreateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.VisitOrderUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.domain.tripplace.entity.TripPlace;
import com.travelapp.backend.domain.tripplace.exception.TripPlaceNotFoundException;
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
    private final TripService tripService;

    @Transactional
    public TripPlaceResponse createTripPlace(Long tripDayId, TripPlaceCreateRequest request) {

        TripDay tripDay = tripDayRepository.findById(tripDayId).orElseThrow(
            () -> new TripDayNotFoundException(tripDayId)
        );

        // TripDay가 속한 Trip의 소유자 확인
        tripService.findTripWithOwnerValidation(tripDay.getTrip().getId());

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

        TripDay tripDay = tripDayRepository.findById(tripDayId).orElseThrow(
            () -> new TripDayNotFoundException(tripDayId)
        );

        tripService.findTripWithOwnerValidation(tripDay.getTrip().getId());

        return tripPlaceRepository.findByTripDay_Id(tripDayId).stream()
            .map(TripPlaceResponse :: of)
            .toList();
    }

    @Transactional
    public TripPlaceResponse updateTripPlace(Long placeId, TripPlaceUpdateRequest request) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripService.findTripWithOwnerValidation(tripPlace.getTripDay().getTrip().getId());

        tripPlace.update(request);
        tripPlaceRepository.save(tripPlace);

        return TripPlaceResponse.of(tripPlace);
    }

    @Transactional
    public TripPlaceResponse updateVisitOrder(Long placeId, VisitOrderUpdateRequest request) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripService.findTripWithOwnerValidation(tripPlace.getTripDay().getTrip().getId());

        Integer oldOrder = tripPlace.getVisitOrder();
        Integer newOrder = request.getVisitOrder();

        // 순서가 변경되지 않았으면 그대로 반환
        if (oldOrder.equals(newOrder)) {
            return TripPlaceResponse.of(tripPlace);
        }

        // 같은 날짜에 속한 다른 장소들의 순서도 함께 조정
        List<TripPlace> placesInSameDay = tripPlaceRepository.findByTripDay_Id(tripPlace.getTripDay().getId());

        if (oldOrder < newOrder) {
            // 아래로 이동: 기존 위치와 새 위치 사이의 항목들은 한 칸씩 위로
            for (TripPlace place : placesInSameDay) {
                if (place.getId().equals(placeId)) continue;

                Integer currentOrder = place.getVisitOrder();
                if (currentOrder > oldOrder && currentOrder <= newOrder) {
                    place.visitOrderUpdate(new VisitOrderUpdateRequest(currentOrder - 1));
                    tripPlaceRepository.save(place);
                }
            }
        } else {
            // 위로 이동: 새 위치와 기존 위치 사이의 항목들은 한칸씩 아래로
            for (TripPlace place : placesInSameDay) {
                if (place.getId().equals(placeId)) continue;

                Integer currentOrder = place.getVisitOrder();
                if (currentOrder >= newOrder && currentOrder < oldOrder) {
                    place.visitOrderUpdate(new VisitOrderUpdateRequest(currentOrder + 1));
                    tripPlaceRepository.save(place);
                }

            }

        }

        tripPlace.visitOrderUpdate(request);
        tripPlaceRepository.save(tripPlace);

        return TripPlaceResponse.of(tripPlace);
    }

    @Transactional
    public void deleteTripPlace(Long placeId) {

        TripPlace tripPlace = existsTripPlace(placeId);

        tripService.findTripWithOwnerValidation(tripPlace.getTripDay().getTrip().getId());

        tripPlaceRepository.delete(tripPlace);
    }

    private TripPlace existsTripPlace(Long placeId) {

        return tripPlaceRepository.findById(placeId).orElseThrow(
            () -> new TripPlaceNotFoundException(placeId)
        );
    }



}
