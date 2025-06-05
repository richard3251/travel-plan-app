package com.travelapp.backend.domain.trip.dto.request;

import com.travelapp.backend.global.validation.ValidDateRange;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;

@Getter
@ValidDateRange
public class TripCreateRequest {

    @NotBlank(message = "여행 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "여행 제목은 1자 이상 100자 이하여야 합니다")
    private String title;

    @NotNull(message = "여행 시작일은 필수입니다")
    @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수입니다")
    private LocalDate endDate;

    @Size(max = 100, message = "지역명은 100자를 초과할 수 없습니다")
    private String region;

    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    private Double regionLat;

    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private Double regionLng;
}
