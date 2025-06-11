package com.travelapp.backend.domain.trip.controller;

import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.global.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<Void> createTrip(
        @Valid @RequestBody TripCreateRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        tripService.createTrip(memberId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getTrips() {

        Long memberId = SecurityUtil.getCurrentMemberId();

        return ResponseEntity.ok(tripService.getTrips(memberId));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(
        @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTrip(tripId));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<Void> modifyTrip(
        @PathVariable Long tripId,
        @Valid @RequestBody TripModifyRequest request
    ) {
        tripService.modifyTrip(tripId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
        @PathVariable Long tripId
    ) {
        tripService.deleteTrip(tripId);

        return ResponseEntity.ok().build();
    }


}
