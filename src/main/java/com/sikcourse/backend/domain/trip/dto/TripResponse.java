package com.sikcourse.backend.domain.trip.dto;

import com.sikcourse.backend.domain.trip.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TripResponse(
        Long tripId,
        String title,
        String areaCode,
        String sigunguCode,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseMapX,
        BigDecimal baseMapY,
        String aiCourseMessage,
        String courseFeedbackMessage
) {

    public static TripResponse from(Trip trip) {
        return from(trip, null, null);
    }

    public static TripResponse from(Trip trip, String aiCourseMessage, String courseFeedbackMessage) {
        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getAreaCode(),
                trip.getSigunguCode(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getBaseMapX(),
                trip.getBaseMapY(),
                aiCourseMessage,
                courseFeedbackMessage
        );
    }
}
