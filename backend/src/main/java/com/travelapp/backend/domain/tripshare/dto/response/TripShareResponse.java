package com.travelapp.backend.domain.tripshare.dto.response;

import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.tripshare.entity.TripShare;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "여행 공유 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripShareResponse {

    @Schema(description = "공유 ID", example = "1")
    private Long id;

    @Schema(description = "공유 토큰", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String shareToken;

    @Schema(description = "공유 URL", example = "https://travelapp.com/shared/a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String shareUrl;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "공유 생성 시간", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "공유 만료 시간", example = "2024-12-31T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "조회 수", example = "150")
    private Integer viewCount;

    @Schema(description = "여행 정보", example = "25")
    private TripResponse trip;

    @Schema(description = "만료 여부", example = "false")
    private Boolean isExpired;

    public static TripShareResponse of(TripShare tripShare, String baseUrl) {
        return TripShareResponse.builder()
            .id(tripShare.getId())
            .shareToken(tripShare.getShareToken())
            .shareUrl(baseUrl + "/shared/" + tripShare.getShareToken())
            .isPublic(tripShare.getIsPublic())
            .createdAt(tripShare.getCreatedAt())
            .expiryDate(tripShare.getExpiryDate())
            .viewCount(tripShare.getViewCount())
            .trip(TripResponse.of(tripShare.getTrip()))
            .isExpired(tripShare.isExpired())
            .build();
    }

}
