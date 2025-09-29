package com.travelapp.backend.global.validation;


import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        if (value == null) {
            return true; // null 값은 @NotNull로 별도 처리
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (value instanceof TripCreateRequest) {
            TripCreateRequest request = (TripCreateRequest) value;
            startDate = request.getStartDate();
            endDate = request.getEndDate();
        } else if (value instanceof TripModifyRequest) {
            TripModifyRequest request = (TripModifyRequest) value;
            startDate = request.getStartDate();
            endDate = request.getEndDate();
        }

        if (startDate == null || endDate == null) {
            return true; // null 값은 다른 검증에서 처리
        }

        return !endDate.isBefore(startDate);
    }

}
