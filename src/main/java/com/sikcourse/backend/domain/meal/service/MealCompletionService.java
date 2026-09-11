package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.meal.dto.CompleteMealRequest;
import com.sikcourse.backend.domain.meal.dto.CompleteMealResponse;
import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.meal.entity.MenuType;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.domain.trip.error.TripErrorCode;
import com.sikcourse.backend.domain.trip.repository.TripRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealCompletionService {

    private final MealRecordService mealRecordService;
    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public CompleteMealResponse complete(Long userId, CompleteMealRequest request) {
        Trip trip = tripRepository.findByIdAndUserId(request.tripId(), userId)
                .orElseThrow(() -> new GeneralException(TripErrorCode.TRIP_NOT_FOUND));

        MealRecordResponse mealRecord = mealRecordService.create(userId, request.toMealRecordRequest());
        DailyNutritionSummaryResponse nutritionSummary = mealRecordService.getTodaySummary(userId);
        List<Long> tripPlaceIds = findTripPlaces(trip).stream()
                .map(Place::getId)
                .toList();

        boolean dessertAvailable = !tripPlaceIds.isEmpty()
                && menuRepository.existsByPlaceIdInAndMenuType(tripPlaceIds, MenuType.DESSERT);
        boolean walkAvailable = !findTripWalkPlaces(trip).isEmpty();

        return new CompleteMealResponse(
                mealRecord,
                nutritionSummary,
                dessertAvailable,
                walkAvailable
        );
    }

    private List<Place> findTripPlaces(Trip trip) {
        if (trip.getSigunguCode() == null || trip.getSigunguCode().isBlank()) {
            return placeRepository.findAllByAreaCodeOrderByTitleAsc(trip.getAreaCode());
        }
        return placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc(
                trip.getAreaCode(),
                trip.getSigunguCode()
        );
    }

    private List<Place> findTripWalkPlaces(Trip trip) {
        if (trip.getSigunguCode() == null || trip.getSigunguCode().isBlank()) {
            return placeRepository.findAllByAreaCodeAndPlaceTypeOrderByTitleAsc(trip.getAreaCode(), PlaceType.WALK);
        }
        return placeRepository.findAllByAreaCodeAndSigunguCodeAndPlaceTypeOrderByTitleAsc(
                trip.getAreaCode(),
                trip.getSigunguCode(),
                PlaceType.WALK
        );
    }
}
