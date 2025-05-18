package com.travelapp.backend.domain.tripday.service;

import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.repository.TripRepository;
import com.travelapp.backend.domain.tripday.dto.request.TripDayCreateRequest;
import com.travelapp.backend.domain.tripday.dto.response.TripDayResponse;
import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripday.repository.TripDayRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripDayService {

    private final TripDayRepository tripDayRepository;
    private final TripRepository tripRepository;

    @Transactional
    public TripDayResponse createTripDay(Long tripId, TripDayCreateRequest request) {

        Trip trip = tripRepository.findById(tripId).orElseThrow(
            () -> new IllegalArgumentException("해당 여행일정이 없습니다.")
        );

        TripDay tripDay = TripDay.builder()
            .trip(trip)
            .day(request.getDay())
            .date(request.getDate())
            .build();

        return TripDayResponse.of(tripDayRepository.save(tripDay));
    }

    @Transactional
    public List<TripDayResponse> getTripDays(Long tripId) {

        Trip trip = tripRepository.findById(tripId).orElseThrow(
            () -> new IllegalArgumentException("해당 여행일정이 없습니다.")
        );

        return tripDayRepository.findByTrip(trip).stream()
            .map(TripDayResponse :: of)
            .toList();
    }

    @Transactional
    public void deleteTripDay(Long dayId) {
        TripDay tripDay = tripDayRepository.findById(dayId).orElseThrow(
            () -> new IllegalArgumentException("해당 여행 날짜가 존재하지 않습니다.")
        );

        tripDayRepository.delete(tripDay);
    }




}
