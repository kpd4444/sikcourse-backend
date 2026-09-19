package com.sikcourse.backend.domain.health.dto;

import com.sikcourse.backend.domain.health.entity.BmiStatus;

public record RecommendedHealthGoalsResponse(
        Integer dailyCalorieGoal,
        Integer dailySodiumGoal,
        Integer dailySugarGoal,
        Double bmi,
        BmiStatus bmiStatus
) {
}
