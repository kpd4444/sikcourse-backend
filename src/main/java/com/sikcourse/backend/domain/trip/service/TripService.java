package com.sikcourse.backend.domain.trip.service;

import com.sikcourse.backend.domain.message.service.GeminiMessageService;
import com.sikcourse.backend.domain.trip.dto.CreateTripRequest;
import com.sikcourse.backend.domain.trip.dto.TripResponse;
import com.sikcourse.backend.domain.trip.dto.UpdateTripRequest;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.domain.trip.error.TripErrorCode;
import com.sikcourse.backend.domain.trip.repository.TripRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final GeminiMessageService geminiMessageService;

    @Transactional
    public TripResponse create(Long userId, CreateTripRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        Trip trip = Trip.builder()
                .userId(userId)
                .title(request.title())
                .areaCode(request.areaCode())
                .sigunguCode(request.sigunguCode())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .baseMapX(request.baseMapX())
                .baseMapY(request.baseMapY())
                .build();

        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getTrips(Long userId) {
        return tripRepository.findAllByUserIdOrderByStartDateDescIdDesc(userId).stream()
                .map(TripResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(Long userId, Long tripId) {
        Trip trip = getByIdAndUserId(tripId, userId);
        return TripResponse.from(
                trip,
                geminiMessageService.tripCourseMessage(trip),
                geminiMessageService.tripCourseFeedbackMessage(trip)
        );
    }

    @Transactional
    public TripResponse update(Long userId, Long tripId, UpdateTripRequest request) {
        Trip trip = getByIdAndUserId(tripId, userId);
        LocalDate startDate = request.startDate() == null ? trip.getStartDate() : request.startDate();
        LocalDate endDate = request.endDate() == null ? trip.getEndDate() : request.endDate();
        validateDateRange(startDate, endDate);

        trip.update(
                request.title(),
                request.areaCode(),
                request.sigunguCode(),
                request.startDate(),
                request.endDate(),
                request.baseMapX(),
                request.baseMapY()
        );
        return TripResponse.from(trip);
    }

    @Transactional
    public void delete(Long userId, Long tripId) {
        Trip trip = getByIdAndUserId(tripId, userId);
        tripRepository.delete(trip);
    }

    private Trip getByIdAndUserId(Long tripId, Long userId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new GeneralException(TripErrorCode.TRIP_NOT_FOUND));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new GeneralException(TripErrorCode.INVALID_TRIP_DATE_RANGE);
        }
    }
}
