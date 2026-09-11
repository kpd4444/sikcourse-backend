package com.sikcourse.backend.domain.trip.service;

import com.sikcourse.backend.domain.trip.dto.CreateTripRequest;
import com.sikcourse.backend.domain.trip.dto.UpdateTripRequest;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.domain.trip.error.TripErrorCode;
import com.sikcourse.backend.domain.trip.repository.TripRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripServiceTest {

    @Test
    void createRejectsStartDateAfterEndDate() {
        TripService tripService = new TripService(mock(TripRepository.class));
        CreateTripRequest request = new CreateTripRequest(
                "Jeju Trip",
                "39",
                "4",
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 20),
                new BigDecimal("126.5312"),
                new BigDecimal("33.4996")
        );

        assertThatThrownBy(() -> tripService.create(1L, request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(TripErrorCode.INVALID_TRIP_DATE_RANGE));
    }

    @Test
    void updateValidatesDateRangeWithExistingTripDates() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripService tripService = new TripService(tripRepository);
        Trip trip = trip();

        when(tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip));

        UpdateTripRequest request = new UpdateTripRequest(
                null,
                null,
                null,
                LocalDate.of(2026, 9, 23),
                null,
                null,
                null
        );

        assertThatThrownBy(() -> tripService.update(1L, 1L, request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(TripErrorCode.INVALID_TRIP_DATE_RANGE));
    }

    @Test
    void getTripUsesUserIdOwnershipCondition() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripService tripService = new TripService(tripRepository);

        when(tripRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getTrip(2L, 1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(TripErrorCode.TRIP_NOT_FOUND));
    }

    @Test
    void createReturnsSavedTrip() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripService tripService = new TripService(tripRepository);

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(tripService.create(1L, createRequest()).title()).isEqualTo("Jeju Trip");
    }

    private CreateTripRequest createRequest() {
        return new CreateTripRequest(
                "Jeju Trip",
                "39",
                "4",
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 22),
                new BigDecimal("126.5312"),
                new BigDecimal("33.4996")
        );
    }

    private Trip trip() {
        return Trip.builder()
                .userId(1L)
                .title("Jeju Trip")
                .areaCode("39")
                .sigunguCode("4")
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .baseMapX(new BigDecimal("126.5312"))
                .baseMapY(new BigDecimal("33.4996"))
                .build();
    }
}
