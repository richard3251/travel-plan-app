package com.travelapp.backend.domain.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Pre-signed URL 발급 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequest {

    @Schema(description = "원본 파일명", example = "vacation_photo.jpg", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "파일명은 필수입니다")
    private String fileName;

    @Schema(description = "파일 크기 (바이트)", example = "2048576", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "파일 크기는 필수입니다")
    @Positive(message = "파일 크기는 0보다 커야 합니다")
    private Long fileSize;

    @Schema(description = "콘텐츠 타입", example = "image/jpeg", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "콘텐츠 타입은 필수입니다")
    private String contentType;

    @Schema(description = "여행 ID (여행 이미지 업로드시)", example = "1")
    private Long tripId;

    @Schema(description = "커버 이미지 여부", example = "false")
    @Builder.Default
    private Boolean isCoverImage = false;

    @Schema(description = "이미지 캡션", example = "아름다운 일몰")
    private String caption;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;
}
