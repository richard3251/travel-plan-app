package com.travelapp.backend.domain.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "업로드 완료 통지 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadCompleteRequest {

    @Schema(description = "파일 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "파일 ID는 필수입니다")
    private Long fileId;

    @Schema(description = "업로드 성공 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "업로드 성공 여부는 필수입니다")
    private Boolean success;

    @Schema(description = "실패 사유 (실패시)", example = "Network error occurred")
    private String errorMessage;
}
