package com.travelapp.backend.domain.tripplace.controller;

import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceCreateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.VisitOrderUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.response.TripPlaceResponse;
import com.travelapp.backend.domain.tripplace.service.TripPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "여행지 관리", description = "여행 일자별 여행지 생성, 조회, 수정, 삭제, 순서 변경 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip-days/{tripDayId}/places")
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    @Operation(summary = "여행지 추가", description = "여행 일자에 새로운 여행지를 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 추가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<TripPlaceResponse> createTripPlace(
        @Parameter(description = "여행 일자 ID") @PathVariable Long tripDayId,
        @Parameter(description = "여행지 생성 요청 정보") @Valid @RequestBody TripPlaceCreateRequest request
    ) {
        TripPlaceResponse response = tripPlaceService.createTripPlace(tripDayId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행지 목록 조회", description = "특정 여행 일자의 모든 여행지를 순서대로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<List<TripPlaceResponse>> getTripPlaces(
        @Parameter(description = "여행 일자 ID") @PathVariable Long tripDayId
    ) {

        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripDayId);

        return ResponseEntity.ok(tripPlaces);
    }

    @Operation(summary = "여행지 정보 수정", description = "여행지의 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 정보 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행지를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PutMapping("/{placeId}")
    public ResponseEntity<TripPlaceResponse> updateTripPlace(
        @Parameter(description = "여행지 ID") @PathVariable Long placeId,
        @Parameter(description = "여행지 수정 요청 정보") @Valid @RequestBody TripPlaceUpdateRequest request
    ) {

        TripPlaceResponse response = tripPlaceService.updateTripPlace(placeId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행지 순서 변경", description = "여행지의 방문 순서를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 순서 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행지를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{placeId}/order")
    public ResponseEntity<TripPlaceResponse> updateVisitOrder(
        @Parameter(description = "여행지 ID") @PathVariable Long placeId,
        @Parameter(description = "순서 변경 요청 정보") @Valid @RequestBody VisitOrderUpdateRequest request
    ) {
        TripPlaceResponse response = tripPlaceService.updateVisitOrder(placeId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행지 삭제", description = "여행지를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행지 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행지를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deleteTripPlace(
        @Parameter(description = "여행지 ID") @PathVariable Long placeId
    ) {
        tripPlaceService.deleteTripPlace(placeId);

        return ResponseEntity.ok().build();
    }


}
