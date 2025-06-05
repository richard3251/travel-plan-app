package com.travelapp.backend.domain.tripplace.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VisitOrderUpdateRequest {

    @NotNull(message = "방문 순서는 필수입니다")
    @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다")
    private Integer visitOrder;
}
