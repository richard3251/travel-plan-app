package com.travelapp.backend.domain.tripday.controller;

import com.travelapp.backend.domain.tripday.dto.request.TripDayCreateRequest;
import com.travelapp.backend.domain.tripday.dto.response.TripDayResponse;
import com.travelapp.backend.domain.tripday.service.TripDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "여행 일자 관리", description = "여행 일자별 생성, 조회, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/days")
public class TripDayController {

    private final TripDayService tripDayService;

    @Operation(summary = "여행 일자 생성", description = "특정 여행에 새로운 일자를 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 일자 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<TripDayResponse> createTripDay(
        @Parameter(description = "여행 ID") @PathVariable Long tripId,
        @Parameter(description = "여행 일자 생성 요청 정보") @RequestBody TripDayCreateRequest request
    ) {

        TripDayResponse response = tripDayService.createTripDay(tripId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행 일자 목록 조회", description = "특정 여행의 모든 일자를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 일자 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<List<TripDayResponse>> getTripDays(
        @Parameter(description = "여행 ID") @PathVariable Long tripId
    ) {

        List<TripDayResponse> tripDays = tripDayService.getTripDays(tripId);

        return ResponseEntity.ok(tripDays);
    }

    @Operation(summary = "여행 일자 삭제", description = "여행 일자를 삭제합니다. 관련된 모든 여행지도 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 일자 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행 일자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{dayId}")
    public ResponseEntity<Void> deleteTripDay(
        @Parameter(description = "여행 ID") @PathVariable Long tripId,
        @Parameter(description = "여행 일자 ID") @PathVariable Long dayId
    ) {

        tripDayService.deleteTripDay(dayId);

        return ResponseEntity.ok().build();
    }



}
