package com.travelapp.backend.domain.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Pre-signed URL 발급 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {

    @Schema(description = "파일 ID", example = "1")
    private Long fileId;

    @Schema(description = "Pre-signed URL", example = "https://bucket.s3.amazonaws.com/uploads/...")
    private String presignedUrl;

    @Schema(description = "S3 키", example = "uploads/2024/12/01/uuid_filename.jpg")
    private String s3Key;

    @Schema(description = "업로드에 필요한 헤더 정보")
    private UploadHeaders headers;

    @Schema(description = "URL 만료 시간", example = "2024-12-01T12:39:56")
    private LocalDateTime expiresAt;

    @Schema(description = "업로드 가이드")
    private UploadGuide guide;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadHeaders {

        @Schema(description = "Content-Type 헤더", example = "image/jpeg")
        private String contentType;

        @Schema(description = "Content-Type 헤더", example = "2048576")
        private Long contentLength;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadGuide {

        @Schema(description = "HTTP 메서드", example = "PUT")
        private String method;

        @Schema(description = "업로드 완료 후 호출할 API", example = "POST /api/files/upload-complete")
        private String completeApi;

        @Schema(description = "업로드 제한 시간 (분)", example = "5")
        private Integer timeoutMinutes;
    }
}
