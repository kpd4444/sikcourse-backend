package com.sikcourse.backend.domain.meal.dto;

public record CompleteMealResponse(
        MealRecordResponse mealRecord,
        DailyNutritionSummaryResponse nutritionSummary,
        boolean dessertAvailable,
        boolean walkAvailable
) {
}
