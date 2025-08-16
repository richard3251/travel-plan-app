package com.travelapp.backend.domain.tripplace.dto.response;

import com.travelapp.backend.domain.tripplace.entity.TripPlace;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "여행지 정보 응답 DTO")
@Builder
@AllArgsConstructor
@Getter
public class TripPlaceResponse {

    @Schema(description = "여행지 ID", example = "1")
    private Long id;

    @Schema(description = "카카오 장소 ID", example = "27338954")
    private String placeId;

    @Schema(description = "장소명", example = "성산일출봉")
    private String placeName;

    @Schema(description = "주소", example = "제주특별자치도 서귀포시 성산읍 성산리")
    private String address;

    @Schema(description = "위도", example = "33.4584")
    private Double latitude;

    @Schema(description = "경도", example = "126.9410")
    private Double longitude;

    @Schema(description = "메모", example = "일출 명소")
    private String memo;

    @Schema(description = "방문 예정 시간", example = "06:00:00")
    private LocalTime visitTime;

    @Schema(description = "방문 순서", example = "1")
    private Integer visitOrder;

    private TripPlaceResponse() {}

    public static TripPlaceResponse of(TripPlace tripPlace) {
        
        return TripPlaceResponse.builder()
            .id(tripPlace.getId())
            .placeId(tripPlace.getPlaceId())
            .placeName(tripPlace.getPlaceName())
            .address(tripPlace.getAddress())
            .latitude(tripPlace.getLatitude())
            .longitude(tripPlace.getLongitude())
            .memo(tripPlace.getMemo())
            .visitTime(tripPlace.getVisitTime())
            .visitOrder(tripPlace.getVisitOrder())
            .build();
    }

}
