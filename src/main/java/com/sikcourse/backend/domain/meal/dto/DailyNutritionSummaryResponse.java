package com.sikcourse.backend.domain.meal.dto;

public record DailyNutritionSummaryResponse(
        Integer calorieGoal,
        Integer calorieConsumed,
        Integer calorieRemaining,
        Integer sodiumGoal,
        Integer sodiumConsumed,
        Integer sodiumRemaining,
        Integer sugarGoal,
        Integer sugarConsumed,
        Integer sugarRemaining,
        String carePointMessage
) {

    public DailyNutritionSummaryResponse(
            Integer calorieGoal,
            Integer calorieConsumed,
            Integer calorieRemaining,
            Integer sodiumGoal,
            Integer sodiumConsumed,
            Integer sodiumRemaining,
            Integer sugarGoal,
            Integer sugarConsumed,
            Integer sugarRemaining
    ) {
        this(
                calorieGoal,
                calorieConsumed,
                calorieRemaining,
                sodiumGoal,
                sodiumConsumed,
                sodiumRemaining,
                sugarGoal,
                sugarConsumed,
                sugarRemaining,
                null
        );
    }
}
