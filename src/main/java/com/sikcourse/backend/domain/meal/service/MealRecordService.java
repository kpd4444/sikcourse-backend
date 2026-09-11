package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.domain.meal.dto.CreateMealRecordRequest;
import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.repository.MealRecordRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealRecordService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MealRecordRepository mealRecordRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MenuService menuService;

    @Transactional
    public MealRecordResponse create(Long userId, CreateMealRecordRequest request) {
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

        return new DailyNutritionSummaryResponse(
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
    }

    private List<MealRecord> findTodayRecords(Long userId) {
        LocalDate today = LocalDate.now(SERVICE_ZONE_ID);
        LocalDateTime startInclusive = today.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();
        return mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                userId,
                startInclusive,
                endExclusive
        );
    }
}
