package com.travelapp.backend.domain.tripshare.controller;

import com.travelapp.backend.domain.tripshare.dto.request.TripShareCreateRequest;
import com.travelapp.backend.domain.tripshare.dto.response.TripShareResponse;
import com.travelapp.backend.domain.tripshare.service.TripShareService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 공유 관리", description = "여행 공유 링크 생성, 조회, 수정, 삭제 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip-shares")
public class TripShareController {

    private final TripShareService tripShareService;

    /**
     * 여행 공유 링크 생성
     *
     * @param tripId  공유할 여행 ID
     * @param request 공유 설정 정보
     * @return 생성된 공유 링크 정보
     */
    @Operation(
        summary = "여행 공유 링크 생성",
        description = "특정 여행에 대한 공유 링크를 생성합니다. 본인의 여행만 공유할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공유 링크 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 이미 공유된 여행"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 여행 아님)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 여행")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/trips/{tripId}")
    public ResponseEntity<TripShareResponse> createTripShare(
        @Parameter(description = "공유할 여행 ID", example = "1")
        @PathVariable Long tripId,
        @Parameter(description = "공유 설정 정보")
        @Valid @RequestBody TripShareCreateRequest request
    ) {
        log.info("여행 공유 링크 생성 요청 - 여행 ID: {}", tripId);

        Long memberId = SecurityUtil.getCurrentMemberId();
        TripShareResponse response = tripShareService.createSharingLink(tripId, memberId, request);

        log.info("여행 공유 링크 생성 성공 - 공유 ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 공유 토큰으로 여행 조회
     *
     * @param shareToken 공유 토큰
     * @return 공유된 여행 정보
     */
    @Operation(
        summary = "공유된 여행 조회",
        description = "공유 토큰을 통해 공유된 여행 정보를 조회합니다. 조회 시 조회수가 증가합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공유된 여행 조회 성공"),
        @ApiResponse(responseCode = "403", description = "비공개 여행이거나 만료된 링크"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 공유 토큰")
    })
    @GetMapping("/shared/{shareToken}")
    public ResponseEntity<TripShareResponse> getSharedTrip(
        @Parameter(description = "공유 토큰", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable String shareToken
    ) {
        log.info("공유된 여행 조회 요청 - 토큰: {}", shareToken);

        TripShareResponse response = tripShareService.getSharedTrip(shareToken);

        log.info("공유된 여행 조회 성공 - 여행 ID: {}", response.getTrip().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 공유한 여행 목록 조회
     *
     * @return 내가 생성한 공유 링크 목록
     */
    @Operation(
        summary = "내 공유 여행 목록 조회",
        description = "현재 로그인한 사용자가 생성한 모든 공유 링크 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공유 여행 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my-shares")
    public ResponseEntity<List<TripShareResponse>> getMySharedTrips() {
        log.info("내 공유 여행 목록 조회 요청");

        Long memberId = SecurityUtil.getCurrentMemberId();
        List<TripShareResponse> responses = tripShareService.getMySharedTrips(memberId);

        log.info("내 공유 여행 목록 조회 성공 - 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * 공개 공유 여행 목록 조회 (페이징)
     *
     * @param pageable 페이징 정보
     * @param sortBy   정렬 기준 (latest: 최신순, popular: 인기순)
     * @return 공개된 공유 여행 목록
     */
    @Operation(
        summary = "공개 공유 여행 목록 조회",
        description = "공개된 모든 공유 여행 목록을 페이징하여 조회합니다. 최신순 또는 인기순으로 정렬 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공개 공유 여행 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 정렬 기준")
    })
    @GetMapping("/public")
    public ResponseEntity<Page<TripShareResponse>> getPublicSharedTrips(
        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
        Pageable pageable,
        @Parameter(description = "정렬 기준", example = "latest")
        @RequestParam(defaultValue = "latest") String sortBy
    ) {
        log.info("공개 공유 여행 목록 조회 요청 - 정렬: {}, 페이지: {}", sortBy, pageable.getPageNumber());

        Page<TripShareResponse> responses = tripShareService.getPublicSharedTrips(pageable, sortBy);

        log.info("공개 공유 여행 목록 조회 성공 - 총 개수: {}", responses.getTotalElements());
        return ResponseEntity.ok(responses);
    }

    /**
     * 여행 공유 설정 수정
     *
     * @param tripId 수정할 여행 ID
     * @param request 수정할 공유 설정 정보
     * @return 수정된 공유 정보
     */
    @Operation(
        summary = "여행 공유 설정 수정",
        description = "기본 공유 링크의 설정(공개 여부, 만료일 등)을 수정합니다. 본인의 여행만 수정할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공유 설정 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 여행 아님)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 여행 또는 공유 정보")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PutMapping("/trips/{tripId}")
    public ResponseEntity<TripShareResponse> updateTripShare(
        @Parameter(description = "수정할 여행 ID", example = "1")
        @PathVariable Long tripId,
        @Parameter(description = "수정할 공유 설정 정보")
        @Valid @RequestBody TripShareCreateRequest request
    ) {
        log.info("여행 공유 설정 수정 요청 - 여행 ID: {}", tripId);

        Long memberId = SecurityUtil.getCurrentMemberId();
        TripShareResponse response = tripShareService.updateTripShare(tripId, memberId, request);

        log.info("여행 공유 설정 수정 성공 - 공유 ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 여행 공유 삭제
     * @param tripId 삭제할 여행 ID
     * @return 성공 응답
     */
    @Operation(
        summary = "여행 공유 삭제",
        description = "기존 공유 링크를 삭제합니다. 본인의 여행만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공유 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 여행 아님)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 여행 또는 공유 정보")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/trips/{tripId}")
    public ResponseEntity<Void> deleteTripShare(
        @Parameter(description = "삭제할 여행 ID", example = "1")
        @PathVariable Long tripId
    ) {
        log.info("여행 공유 삭제 요청 - 여행 ID: {}", tripId);

        Long memberId = SecurityUtil.getCurrentMemberId();
        tripShareService.deleteTripShare(tripId, memberId);

        return ResponseEntity.ok().build();
    }
}
