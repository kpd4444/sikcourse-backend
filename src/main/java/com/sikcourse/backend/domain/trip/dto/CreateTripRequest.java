package com.sikcourse.backend.domain.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTripRequest(
        @NotBlank(message = "Trip title is required.")
        @Size(max = 100, message = "Trip title must be 100 characters or less.")
        String title,

        @NotBlank(message = "Area code is required.")
        @Size(max = 20, message = "Area code must be 20 characters or less.")
        String areaCode,

        @Size(max = 20, message = "Sigungu code must be 20 characters or less.")
        String sigunguCode,

        @NotNull(message = "Start date is required.")
        LocalDate startDate,

        @NotNull(message = "End date is required.")
        LocalDate endDate,

        BigDecimal baseMapX,
        BigDecimal baseMapY
) {
}
