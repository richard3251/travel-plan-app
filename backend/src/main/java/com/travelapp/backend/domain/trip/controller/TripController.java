package com.travelapp.backend.domain.trip.controller;

import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.global.util.SecurityUtil;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 관리", description = "여행 생성, 조회, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    @Operation(summary = "여행 생성", description = "새로운 여행을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<Void> createTrip(
        @Parameter(description = "여행 생성 요청 정보") @Valid @RequestBody TripCreateRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        tripService.createTrip(memberId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 여행 목록 조회", description = "로그인한 사용자의 모든 여행을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<List<TripResponse>> getTrips() {

        Long memberId = SecurityUtil.getCurrentMemberId();

        return ResponseEntity.ok(tripService.getTrips(memberId));
    }

    @Operation(summary = "여행 상세 조회", description = "특정 여행의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 여행을 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(
        @Parameter(description = "여행 ID") @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTrip(tripId));
    }

    @Operation(summary = "여행 수정", description = "여행의 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PutMapping("/{tripId}")
    public ResponseEntity<Void> modifyTrip(
        @Parameter(description = "여행 ID") @PathVariable Long tripId,
        @Parameter(description = "여행 수정 요청 정보") @Valid @RequestBody TripModifyRequest request
    ) {
        tripService.modifyTrip(tripId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "여행 삭제", description = "여행을 삭제합니다. 관련된 모든 데이터가 함께 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "여행 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "엑세스 권한 없음"),
        @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
        @Parameter(description = "여행 ID") @PathVariable Long tripId
    ) {
        tripService.deleteTrip(tripId);

        return ResponseEntity.ok().build();
    }


}
