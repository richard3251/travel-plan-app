package com.travelapp.backend.domain.tripday.service;

import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.domain.tripday.dto.request.TripDayCreateRequest;
import com.travelapp.backend.domain.tripday.dto.response.TripDayResponse;
import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripday.exception.TripDayNotFoundException;
import com.travelapp.backend.domain.tripday.repository.TripDayRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripDayService {

    private final TripDayRepository tripDayRepository;
    private final TripService tripService;

    @Transactional
    public TripDayResponse createTripDay(Long tripId, TripDayCreateRequest request) {

        Trip trip = tripService.findTripWithOwnerValidation(tripId);

        TripDay tripDay = TripDay.builder()
            .trip(trip)
            .day(request.getDay())
            .date(request.getDate())
            .build();

        return TripDayResponse.of(tripDayRepository.save(tripDay));
    }

    @Transactional
    public List<TripDayResponse> getTripDays(Long tripId) {

        Trip trip = tripService.findTripWithOwnerValidation(tripId);

        return tripDayRepository.findByTrip(trip).stream()
            .map(TripDayResponse :: of)
            .toList();
    }

    @Transactional
    public void deleteTripDay(Long dayId) {
        TripDay tripDay = tripDayRepository.findById(dayId).orElseThrow(
            () -> new TripDayNotFoundException(dayId)
        );

        tripService.findTripWithOwnerValidation(tripDay.getTrip().getId());

        tripDayRepository.delete(tripDay);
    }




}
