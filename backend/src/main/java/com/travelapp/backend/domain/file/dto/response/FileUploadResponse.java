package com.travelapp.backend.domain.file.dto.response;

import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.entity.FileType;
import com.travelapp.backend.domain.file.entity.UploadStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "파일 업로드 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    @Schema(description = "파일 ID", example = "1")
    private Long id;

    @Schema(description = "원본 파일명", example = "vacation_photo.jpg")
    private String originalName;

    @Schema(description = "S3 키", example = "uploads/2024/12/01/uuid_filename.jpg")
    private String s3Key;

    @Schema(description = "파일 URL", example = "https://bucket.s3.amazonaws.com/uploads/...")
    private String fileUrl;

    @Schema(description = "썸네일 URL", example = "https://bucket.s3.amazonaws.com/thumbnails/...")
    private String thumbnailUrl;

    @Schema(description = "파일 크기 (바이트)", example = "2048576")
    private Long fileSize;

    @Schema(description = "파일 타입", example = "IMAGE")
    private FileType fileType;

    @Schema(description = "콘텐츠 타입", example = "image/jpeg")
    private String contentType;

    @Schema(description = "업로드 상태", example = "COMPLETED")
    private UploadStatus uploadStatus;

    @Schema(description = "생성 시간", example = "2024-12-01T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "업로드 완료 시간", example = "2024-12-01T12:35:10")
    private LocalDateTime uploadedAt;

    public static FileUploadResponse from(FileInfo fileInfo) {
        return FileUploadResponse.builder()
            .id(fileInfo.getId())
            .originalName(fileInfo.getOriginalName())
            .s3Key(fileInfo.getS3Key())
            .fileUrl(fileInfo.getS3Url())
            .thumbnailUrl(fileInfo.getThumbnailUrl())
            .fileSize(fileInfo.getFileSize())
            .fileType(fileInfo.getFileType())
            .contentType(fileInfo.getContentType())
            .uploadStatus(fileInfo.getUploadStatus())
            .createdAt(fileInfo.getCreatedAt())
            .uploadedAt(fileInfo.getUploadedAt())
            .build();
    }
}
