package com.sikcourse.backend.domain.trip.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTripRequest(
        @Size(max = 100, message = "Trip title must be 100 characters or less.")
        String title,

        @Size(max = 20, message = "Area code must be 20 characters or less.")
        String areaCode,

        @Size(max = 20, message = "Sigungu code must be 20 characters or less.")
        String sigunguCode,

        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseMapX,
        BigDecimal baseMapY
) {
}
