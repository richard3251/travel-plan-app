package com.travelapp.backend.domain.place.service;

import com.travelapp.backend.global.kakao.dto.KakaoPlaceSearchResponse;
import com.travelapp.backend.infra.kakao.KakaoPlaceSearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoPlaceSearchClient kakaoPlaceSearchClient;

    public KakaoPlaceSearchResponse search(String keyword, double lat, double lng) {
        return kakaoPlaceSearchClient.searchPlaces(keyword, lat, lng);
    }

}
