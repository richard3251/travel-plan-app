package com.travelapp.backend.domain.trip.dto.response;

import com.travelapp.backend.domain.trip.entity.Trip;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "여행 계획 정보 응답 DTO")
@Builder
@Getter
public class TripResponse {

    @Schema(description = "여행 계획 ID", example = "1")
    private Long id;

    @Schema(description = "여행 제목", example = "제주도 3박 4일 여행")
    private String title;

    @Schema(description = "여행 시작일", example = "2024-12-25")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2024-12-28")
    private LocalDate endDate;

    @Schema(description = "여행 지역", example = "제주도")
    private String region;

    @Schema(description = "지역 중심 위도", example = "33.4996")
    private Double regionLat;

    @Schema(description = "지역 중심 경도", example = "126.5312")
    private Double regionLng;

    public static TripResponse of(Trip trip) {
        return TripResponse.builder()
            .id(trip.getId())
            .title(trip.getTitle())
            .startDate(trip.getStartDate())
            .endDate(trip.getEndDate())
            .region(trip.getRegion())
            .regionLat(trip.getRegionLat())
            .regionLng(trip.getRegionLng())
            .build();
    }

}
