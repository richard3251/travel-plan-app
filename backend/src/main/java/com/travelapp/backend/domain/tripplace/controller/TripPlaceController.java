package com.travelapp.backend.domain.tripplace.controller;

import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceCreateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.VisitOrderUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.domain.tripplace.service.TripPlaceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip-days/{tripDayId}/places")
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    @PostMapping
    public ResponseEntity<TripPlaceResponse> createTripPlace(
        @PathVariable Long tripDayId,
        @Valid @RequestBody TripPlaceCreateRequest request
    ) {
        TripPlaceResponse response = tripPlaceService.createTripPlace(tripDayId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TripPlaceResponse>> getTripPlaces(
        @PathVariable Long tripDayId
    ) {

        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripDayId);

        return ResponseEntity.ok(tripPlaces);
    }

    @PutMapping("/{placeId}")
    public ResponseEntity<TripPlaceResponse> updateTripPlace(
        @PathVariable Long placeId,
        @Valid @RequestBody TripPlaceUpdateRequest request
    ) {

        TripPlaceResponse response = tripPlaceService.updateTripPlace(placeId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{placeId}/order")
    public ResponseEntity<TripPlaceResponse> updateVisitOrder(
        @PathVariable Long placeId,
        @Valid @RequestBody VisitOrderUpdateRequest request
    ) {

        TripPlaceResponse response = tripPlaceService.updateVisitOrder(placeId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long placeId
    ) {
        tripPlaceService.delete(placeId);

        return ResponseEntity.ok().build();
    }


}
