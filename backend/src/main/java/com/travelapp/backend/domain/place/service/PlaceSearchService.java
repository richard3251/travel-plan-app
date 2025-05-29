package com.travelapp.backend.domain.place.service;

import com.travelapp.backend.domain.place.dto.request.PlaceToTripPlaceRequest;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceCreateRequest;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.domain.tripplace.service.TripPlaceService;
import com.travelapp.backend.infra.kakao.dto.KakaoPlaceSearchResponse;
import com.travelapp.backend.infra.kakao.KakaoPlaceSearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoPlaceSearchClient kakaoPlaceSearchClient;
    private final TripPlaceService tripPlaceService;

    public KakaoPlaceSearchResponse search(String keyword, double lat, double lng, int page, int size) {
        // 전달받은 좌표를 기준으로 검색 (지역 제한 없음)
        return kakaoPlaceSearchClient.searchPlaces(keyword, lat, lng, page, size);
    }

    @Transactional
    public TripPlaceResponse saveToTrip(PlaceToTripPlaceRequest request) {

        if (request.getTripDayId() == null) {
            throw new IllegalArgumentException("여행 일자 ID가 필요합니다.");
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("위치 정보(위도/경도) 가 필요합니다.");
        }

        TripPlaceCreateRequest createRequest = TripPlaceCreateRequest.of(request);

        return tripPlaceService.createTripPlace(request.getTripDayId(), createRequest);
    }

}
