package com.sikcourse.backend.domain.suitability.service;

import com.sikcourse.backend.domain.health.entity.DietaryRestrictionType;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.error.MealErrorCode;
import com.sikcourse.backend.domain.meal.repository.MealRecordRepository;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.dto.SuitabilityReasonResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityReasonType;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuSuitabilityService {

    private static final int MAX_SCORE = 100;

    private final HealthProfileRepository healthProfileRepository;
    private final MenuRepository menuRepository;
    private final MealRecordRepository mealRecordRepository;
    private final PlaceRepository placeRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MenuSuitabilityResponse calculate(Long userId, Long menuId) {
        HealthProfile healthProfile = getHealthProfile(userId);
        NutritionConsumed consumed = getTodayConsumed(userId);
        Menu menu = getMenu(menuId);

        return calculate(healthProfile, consumed, menu);
    }

    @Transactional(readOnly = true)
    public List<MenuSuitabilityResponse> calculatePlaceMenus(Long userId, Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        HealthProfile healthProfile = getHealthProfile(userId);
        NutritionConsumed consumed = getTodayConsumed(userId);
        return menuRepository.findAllByPlaceIdOrderByNameAsc(placeId).stream()
                .map(menu -> calculate(healthProfile, consumed, menu))
                .sorted(Comparator
                        .comparing(MenuSuitabilityResponse::score).reversed()
                        .thenComparing(MenuSuitabilityResponse::menuName))
                .toList();
    }

    private MenuSuitabilityResponse calculate(
            HealthProfile healthProfile,
            NutritionConsumed consumed,
            Menu menu
    ) {
        int remainingCalories = healthProfile.getDailyCalorieGoal() - consumed.calories();
        int remainingSodium = healthProfile.getDailySodiumGoal() - consumed.sodium();
        int remainingSugar = healthProfile.getDailySugarGoal() - consumed.sugar();

        List<SuitabilityReasonResponse> reasons = List.of(
                        calorieReason(menu, remainingCalories, healthProfile.getDietaryRestrictions()),
                        sodiumReason(menu, remainingSodium, healthProfile.getDiseases(), healthProfile.getDietaryRestrictions()),
                        sugarReason(menu, remainingSugar, healthProfile.getDiseases(), healthProfile.getDietaryRestrictions())
                ).stream()
                .flatMap(List::stream)
                .toList();

        int totalPenalty = reasons.stream()
                .mapToInt(SuitabilityReasonResponse::penalty)
                .sum();
        int score = Math.max(0, MAX_SCORE - totalPenalty);

        return new MenuSuitabilityResponse(
                menu.getId(),
                menu.getName(),
                score,
                level(score),
                reasons
        );
    }

    private List<SuitabilityReasonResponse> calorieReason(
            Menu menu,
            int remainingCalories,
            Set<DietaryRestrictionType> dietaryRestrictions
    ) {
        if (menu.getCalories() <= remainingCalories) {
            return List.of();
        }

        int exceededAmount = menu.getCalories() - remainingCalories;
        int penalty = penalty(remainingCalories, exceededAmount, 15)
                + (dietaryRestrictions.contains(DietaryRestrictionType.LOW_CALORIE) ? 10 : 0);
        return List.of(new SuitabilityReasonResponse(
                SuitabilityReasonType.CALORIE,
                "칼로리가 오늘 잔여 기준을 초과합니다.",
                penalty,
                exceededAmount
        ));
    }

    private List<SuitabilityReasonResponse> sodiumReason(
            Menu menu,
            int remainingSodium,
            Set<DiseaseType> diseases,
            Set<DietaryRestrictionType> dietaryRestrictions
    ) {
        if (menu.getSodium() <= remainingSodium) {
            return List.of();
        }

        int exceededAmount = menu.getSodium() - remainingSodium;
        int penalty = penalty(remainingSodium, exceededAmount, 20);
        if (diseases.contains(DiseaseType.HYPERTENSION)) {
            penalty += 15;
        }
        if (dietaryRestrictions.contains(DietaryRestrictionType.LOW_SODIUM)) {
            penalty += 10;
        }

        return List.of(new SuitabilityReasonResponse(
                SuitabilityReasonType.SODIUM,
                "나트륨이 오늘 잔여 기준을 초과합니다.",
                penalty,
                exceededAmount
        ));
    }

    private List<SuitabilityReasonResponse> sugarReason(
            Menu menu,
            int remainingSugar,
            Set<DiseaseType> diseases,
            Set<DietaryRestrictionType> dietaryRestrictions
    ) {
        if (menu.getSugar() <= remainingSugar) {
            return List.of();
        }

        int exceededAmount = menu.getSugar() - remainingSugar;
        int penalty = penalty(remainingSugar, exceededAmount, 20);
        if (diseases.contains(DiseaseType.DIABETES)) {
            penalty += 15;
        }
        if (dietaryRestrictions.contains(DietaryRestrictionType.LOW_SUGAR)) {
            penalty += 10;
        }

        return List.of(new SuitabilityReasonResponse(
                SuitabilityReasonType.SUGAR,
                "당류가 오늘 잔여 기준을 초과합니다.",
                penalty,
                exceededAmount
        ));
    }

    private int penalty(int remaining, int exceededAmount, int basePenalty) {
        if (remaining <= 0) {
            return basePenalty + 20;
        }

        int exceededPercent = (int) Math.ceil((double) exceededAmount * 100 / remaining);
        return basePenalty + Math.min(20, (int) Math.ceil((double) exceededPercent / 5));
    }

    private SuitabilityLevel level(int score) {
        if (score >= 90) {
            return SuitabilityLevel.EXCELLENT;
        }
        if (score >= 70) {
            return SuitabilityLevel.GOOD;
        }
        if (score >= 50) {
            return SuitabilityLevel.CAUTION;
        }
        return SuitabilityLevel.DANGER;
    }

    private HealthProfile getHealthProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(HealthErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }

    private Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new GeneralException(MealErrorCode.MENU_NOT_FOUND));
    }

    private NutritionConsumed getTodayConsumed(Long userId) {
        List<MealRecord> mealRecords = findTodayRecords(userId);
        Map<Long, Menu> menusById = mealRecords.stream()
                .map(MealRecord::getMenuId)
                .distinct()
                .map(this::getMenu)
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        int calories = 0;
        int sodium = 0;
        int sugar = 0;
        for (MealRecord mealRecord : mealRecords) {
            Menu menu = menusById.get(mealRecord.getMenuId());
            calories += menu.getCalories();
            sodium += menu.getSodium();
            sugar += menu.getSugar();
        }

        return new NutritionConsumed(calories, sodium, sugar);
    }

    private List<MealRecord> findTodayRecords(Long userId) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startInclusive = today.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();
        return mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                userId,
                startInclusive,
                endExclusive
        );
    }

    private record NutritionConsumed(int calories, int sodium, int sugar) {
    }
}
