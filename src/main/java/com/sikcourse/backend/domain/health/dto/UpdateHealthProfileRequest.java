package com.sikcourse.backend.domain.health.dto;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.Gender;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.Set;

public record UpdateHealthProfileRequest(
        LocalDate birthDate,
        Gender gender,

        @Min(value = 1, message = "키는 1 이상이어야 합니다.")
        Integer height,

        @Min(value = 1, message = "몸무게는 1 이상이어야 합니다.")
        Integer weight,

        ActivityLevel activityLevel,
        Set<DiseaseType> diseases,

        @Min(value = 1, message = "일일 목표 칼로리는 1 이상이어야 합니다.")
        Integer dailyCalorieGoal,

        @Min(value = 1, message = "일일 목표 나트륨은 1 이상이어야 합니다.")
        Integer dailySodiumGoal,

        @Min(value = 1, message = "일일 목표 당류는 1 이상이어야 합니다.")
        Integer dailySugarGoal
) {
}
