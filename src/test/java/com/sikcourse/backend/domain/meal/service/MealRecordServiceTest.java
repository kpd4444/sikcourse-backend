package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.Gender;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.MealType;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.repository.MealRecordRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MealRecordServiceTest {

    @Test
    void getTodaySummaryCalculatesConsumedAndRemainingNutrition() {
        MealRecordRepository mealRecordRepository = mock(MealRecordRepository.class);
        HealthProfileRepository healthProfileRepository = mock(HealthProfileRepository.class);
        MenuService menuService = mock(MenuService.class);
        MealRecordService mealRecordService = new MealRecordService(
                mealRecordRepository,
                healthProfileRepository,
                menuService
        );

        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile()));
        when(mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(
                mealRecord(1L, 1L, MealType.LUNCH),
                mealRecord(2L, 2L, MealType.DINNER)
        ));
        when(menuService.getById(1L)).thenReturn(menu(1L, 420, 780, 3));
        when(menuService.getById(2L)).thenReturn(menu(2L, 650, 900, 12));

        DailyNutritionSummaryResponse response = mealRecordService.getTodaySummary(1L);

        assertThat(response.calorieGoal()).isEqualTo(2000);
        assertThat(response.calorieConsumed()).isEqualTo(1070);
        assertThat(response.calorieRemaining()).isEqualTo(930);
        assertThat(response.sodiumGoal()).isEqualTo(2000);
        assertThat(response.sodiumConsumed()).isEqualTo(1680);
        assertThat(response.sodiumRemaining()).isEqualTo(320);
        assertThat(response.sugarGoal()).isEqualTo(50);
        assertThat(response.sugarConsumed()).isEqualTo(15);
        assertThat(response.sugarRemaining()).isEqualTo(35);
    }

    @Test
    void getTodaySummaryRequiresHealthProfile() {
        MealRecordRepository mealRecordRepository = mock(MealRecordRepository.class);
        HealthProfileRepository healthProfileRepository = mock(HealthProfileRepository.class);
        MenuService menuService = mock(MenuService.class);
        MealRecordService mealRecordService = new MealRecordService(
                mealRecordRepository,
                healthProfileRepository,
                menuService
        );

        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealRecordService.getTodaySummary(1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(HealthErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }

    private HealthProfile healthProfile() {
        return HealthProfile.builder()
                .userId(1L)
                .birthDate(LocalDate.of(1998, 5, 20))
                .gender(Gender.MALE)
                .height(175)
                .weight(70)
                .activityLevel(ActivityLevel.MODERATE)
                .diseases(Set.of())
                .allergies(Set.of())
                .dietaryRestrictions(Set.of())
                .dailyCalorieGoal(2000)
                .dailySodiumGoal(2000)
                .dailySugarGoal(50)
                .build();
    }

    private MealRecord mealRecord(Long recordId, Long menuId, MealType mealType) {
        return MealRecord.builder()
                .userId(1L)
                .menuId(menuId)
                .mealType(mealType)
                .eatenAt(LocalDateTime.of(2026, 9, 11, 12, 30))
                .build();
    }

    private Menu menu(Long menuId, Integer calories, Integer sodium, Integer sugar) {
        return Menu.builder()
                .placeId(1L)
                .name("테스트 메뉴 " + menuId)
                .calories(calories)
                .sodium(sodium)
                .sugar(sugar)
                .build();
    }
}
