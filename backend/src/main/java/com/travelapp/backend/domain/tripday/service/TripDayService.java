package com.travelapp.backend.domain.tripday.service;

import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.repository.TripRepository;
import com.travelapp.backend.domain.tripday.dto.request.TripDayCreateRequest;
import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripday.repository.TripDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripDayService {

    private final TripDayRepository tripDayRepository;
    private final TripRepository tripRepository;

    @Transactional
    public void createTripDay(Long tripId, TripDayCreateRequest request, Integer dayNumber) {

        Trip trip = tripRepository.findById(tripId).orElseThrow(
            () -> new IllegalArgumentException("해당 여행일정이 없습니다.")
        );

        TripDay tripDay = TripDay.builder()
            .trip(trip)
            .day(dayNumber)
            .date(request.getDate())
            .build();

        tripDayRepository.save(tripDay);
    }




}
