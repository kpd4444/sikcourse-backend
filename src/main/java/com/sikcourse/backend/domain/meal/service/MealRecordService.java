package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.domain.meal.dto.CreateMealRecordRequest;
import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.error.MealErrorCode;
import com.sikcourse.backend.domain.meal.repository.MealRecordRepository;
import com.sikcourse.backend.domain.message.service.GeminiMessageService;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealRecordService {

    private final MealRecordRepository mealRecordRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MenuService menuService;
    private final Clock clock;
    private final GeminiMessageService geminiMessageService;

    @Transactional
    public MealRecordResponse create(Long userId, CreateMealRecordRequest request) {
        validateEatenAt(request.eatenAt());
        Menu menu = menuService.getById(request.menuId());
        MealRecord mealRecord = MealRecord.builder()
                .userId(userId)
                .menuId(menu.getId())
                .mealType(request.mealType())
                .eatenAt(request.eatenAt())
                .build();

        return MealRecordResponse.from(mealRecordRepository.save(mealRecord), menu);
    }

    @Transactional(readOnly = true)
    public List<MealRecordResponse> getTodayRecords(Long userId) {
        List<MealRecord> mealRecords = findTodayRecords(userId);
        Map<Long, Menu> menusById = mealRecords.stream()
                .map(MealRecord::getMenuId)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), menuService::getById));

        return mealRecords.stream()
                .map(mealRecord -> MealRecordResponse.from(mealRecord, menusById.get(mealRecord.getMenuId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public DailyNutritionSummaryResponse getTodaySummary(Long userId) {
        HealthProfile healthProfile = healthProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(HealthErrorCode.HEALTH_PROFILE_NOT_FOUND));
        List<MealRecordResponse> records = getTodayRecords(userId);

        int calorieConsumed = records.stream().mapToInt(MealRecordResponse::calories).sum();
        int sodiumConsumed = records.stream().mapToInt(MealRecordResponse::sodium).sum();
        int sugarConsumed = records.stream().mapToInt(MealRecordResponse::sugar).sum();

        DailyNutritionSummaryResponse summary = new DailyNutritionSummaryResponse(
                healthProfile.getDailyCalorieGoal(),
                calorieConsumed,
                healthProfile.getDailyCalorieGoal() - calorieConsumed,
                healthProfile.getDailySodiumGoal(),
                sodiumConsumed,
                healthProfile.getDailySodiumGoal() - sodiumConsumed,
                healthProfile.getDailySugarGoal(),
                sugarConsumed,
                healthProfile.getDailySugarGoal() - sugarConsumed
        );
        return new DailyNutritionSummaryResponse(
                summary.calorieGoal(),
                summary.calorieConsumed(),
                summary.calorieRemaining(),
                summary.sodiumGoal(),
                summary.sodiumConsumed(),
                summary.sodiumRemaining(),
                summary.sugarGoal(),
                summary.sugarConsumed(),
                summary.sugarRemaining(),
                geminiMessageService.dailySummaryMessage(records, summary)
        );
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

    private void validateEatenAt(LocalDateTime eatenAt) {
        if (eatenAt.isAfter(LocalDateTime.now(clock))) {
            throw new GeneralException(MealErrorCode.INVALID_EATEN_AT);
        }
    }
}
