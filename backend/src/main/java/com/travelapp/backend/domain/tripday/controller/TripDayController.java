package com.travelapp.backend.domain.tripday.controller;

import com.travelapp.backend.domain.tripday.dto.request.TripDayCreateRequest;
import com.travelapp.backend.domain.tripday.dto.response.TripDayResponse;
import com.travelapp.backend.domain.tripday.service.TripDayService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/days")
public class TripDayController {

    private final TripDayService tripDayService;

    @PostMapping
    public ResponseEntity<TripDayResponse> createTripDay(
        @PathVariable Long tripId,
        @RequestBody TripDayCreateRequest request
    ) {

        TripDayResponse response = tripDayService.createTripDay(tripId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TripDayResponse>> getTripDays(
        @PathVariable Long tripId
    ) {

        List<TripDayResponse> tripDays = tripDayService.getTripDays(tripId);

        return ResponseEntity.ok(tripDays);
    }

    @DeleteMapping("/{dayId}")
    public ResponseEntity<Void> deleteTripDay(
        @PathVariable Long tripId,
        @PathVariable Long dayId
    ) {

        tripDayService.deleteTripDay(dayId);

        return ResponseEntity.ok().build();
    }



}
