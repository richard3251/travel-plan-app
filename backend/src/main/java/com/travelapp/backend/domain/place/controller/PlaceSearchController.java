package com.travelapp.backend.domain.place.controller;

import com.travelapp.backend.domain.place.dto.request.PlaceToTripPlaceRequest;
import com.travelapp.backend.domain.place.service.PlaceSearchService;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.global.kakao.dto.KakaoPlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/places")
    public ResponseEntity<KakaoPlaceSearchResponse> searchPlaces(
        @RequestParam String keyword,
        @RequestParam double lat,
        @RequestParam double lng
    ) {
        KakaoPlaceSearchResponse response = placeSearchService.search(keyword, lat, lng);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-to-trip")
    public ResponseEntity<TripPlaceResponse> saveSearchResultToTrip(
        @RequestBody PlaceToTripPlaceRequest request
    ) {

        return ResponseEntity.ok(placeSearchService.saveToTrip(request));
    }


}
