package com.sikcourse.backend.infra.aimenu;

public record MenuNutritionMatchResponse(
        String matched_food,
        Double kcal,
        Double sodium_mg,
        Double sugar_g,
        Double saturated_fat_g,
        Double confidence,
        Boolean is_estimated
) {
}
