package com.travelapp.backend.domain.file.dto.response;

import com.travelapp.backend.domain.file.entity.TripImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "여행 이미지 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripImageResponse {

    @Schema(description = "여행 이미지 ID", example = "1")
    private Long id;

    @Schema(description = "여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "파일 정보")
    private FileUploadResponse fileInfo;

    @Schema(description = "커버 이미지 여부", example = "true")
    private Boolean isCoverImage;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "이미지 캡션", example = "아름다운 일몰")
    private String caption;

    @Schema(description = "생성 시간", example = "2024-12-01T12:34:56")
    private LocalDateTime createdAt;

    public static TripImageResponse from(TripImage tripImage) {
        return TripImageResponse.builder()
            .id(tripImage.getId())
            .tripId(tripImage.getTrip().getId())
            .fileInfo(FileUploadResponse.from(tripImage.getFileInfo()))
            .isCoverImage(tripImage.getIsCoverImage())
            .displayOrder(tripImage.getDisplayOrder())
            .caption(tripImage.getCaption())
            .createdAt(tripImage.getCreatedAt())
            .build();
    }

}
