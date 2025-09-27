package com.travelapp.backend.domain.tripshare.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "여행 공유 생성 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripShareCreateRequest {

    @Schema(description = "공개 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "공개 여부는 필수입니다")
    private Boolean isPublic;

    @Schema(description = "공유 만료일 (선택사항)", example = "2025-12-31T23:59:59")
    @Future(message = "만료일은 현재 시간 이후여야 합니다")
    private LocalDateTime expiryDate;
}
