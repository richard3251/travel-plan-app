package com.travelapp.backend.domain.place.controller;

import com.travelapp.backend.domain.place.dto.request.PlaceToTripPlaceRequest;
import com.travelapp.backend.domain.place.service.PlaceSearchService;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.infra.kakao.dto.KakaoPlaceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장소 검색", description = "카카오 API를 통한 장소 검색 및 여행지 추가 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    @Operation(summary = "장소 검색", description = "키워드와 좌표를 기반으로 카카오 API를 통해 장소를 검색합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장소 검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "500", description = "카카오 API 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/places")
    public ResponseEntity<KakaoPlaceSearchResponse> searchPlaces(
        @Parameter(description = "검색 키워드") @RequestParam String keyword,
        @Parameter(description = "위도") @RequestParam double lat,
        @Parameter(description = "경도") @RequestParam double lng,
        @Parameter(description = "페이지 번호 (1~45)") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "페이지 크기 (1~15)") @RequestParam(defaultValue = "15") int size
    )

    {
        KakaoPlaceSearchResponse response = placeSearchService.search(keyword, lat, lng, page, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "검색 결과를 여행지로 추가", description = "카카오 검색 결과를 여행 일정에 여행지로 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 추가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행 일자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/save-to-trip")
    public ResponseEntity<TripPlaceResponse> saveSearchResultToTrip(
        @Parameter(description = "장소를 여행지로 추가하는 요청 정보") @RequestBody PlaceToTripPlaceRequest request
    ) {

        return ResponseEntity.ok(placeSearchService.saveToTrip(request));
    }


}
