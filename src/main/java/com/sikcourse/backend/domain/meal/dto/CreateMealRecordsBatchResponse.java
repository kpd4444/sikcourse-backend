package com.sikcourse.backend.domain.meal.dto;

import java.util.List;

public record CreateMealRecordsBatchResponse(
        List<MealRecordResponse> records,
        Integer totalCalories,
        Integer totalSodium,
        Integer totalSugar,
        DailyNutritionSummaryResponse nutritionSummary
) {
}
