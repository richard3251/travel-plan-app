package com.travelapp.backend.domain.trip.dto.request;

import com.travelapp.backend.global.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Schema(description = "여행 계획 생성 요청 DTO")
@Getter
@ValidDateRange
public class TripCreateRequest {

    @Schema(description = "여행 제목", example = "제주도 3박 4일 여행", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "여행 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "여행 제목은 1자 이상 100자 이하여야 합니다")
    private String title;

    @Schema(description = "여행 시작일", example = "2024-12-25", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "여행 시작일은 필수입니다")
    @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2024-12-28", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "여행 종료일은 필수입니다")
    private LocalDate endDate;

    @Schema(description = "여행 지역", example = "제주도")
    @Size(max = 100, message = "지역명은 100자를 초과할 수 없습니다")
    private String region;

    @Schema(description = "지역 중심 위도", example = "33.4996")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    private Double latitude;

    @Schema(description = "지역 중심 경도", example = "126.5312")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private Double longitude;
}
