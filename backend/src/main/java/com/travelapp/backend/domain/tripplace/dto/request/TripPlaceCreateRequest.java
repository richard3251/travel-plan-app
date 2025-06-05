package com.travelapp.backend.domain.tripplace.dto.request;

import com.travelapp.backend.domain.place.dto.request.PlaceToTripPlaceRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPlaceCreateRequest {

    @NotBlank(message = "장소명은 필수입니다")
    @Size(max = 200, message = "장소명은 200자를 초과할 수 없습니다")
    private String placeName;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 300, message = "주소는 300자를 초과할 수 없습니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private Double longitude;

    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    private String memo;

    @Size(max = 100, message = "장소 ID는 100자를 초과할 수 없습니다")
    private String placeId;

    @NotNull(message = "방문 시간은 필수입니다")
    private LocalTime visitTime;

    @NotNull(message = "방문 순서는 필수입니다")
    @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다")
    private Integer visitOrder;

    public static TripPlaceCreateRequest of(PlaceToTripPlaceRequest request) {
        return TripPlaceCreateRequest.builder()
            .placeName(request.getPlaceName())
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .memo(request.getMemo())
            .placeId(request.getPlaceId())
            .visitTime(request.getVisitTime())
            .visitOrder(request.getVisitOrder())
            .build();
    }

}
