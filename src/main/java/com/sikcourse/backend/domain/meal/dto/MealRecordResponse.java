package com.sikcourse.backend.domain.meal.dto;

import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.MealType;
import com.sikcourse.backend.domain.meal.entity.Menu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record MealRecordResponse(
        Long mealRecordId,
        Long menuId,
        String menuName,
        MealType mealType,
        LocalDateTime eatenAt,
        BigDecimal servingAmount,
        Integer calories,
        Integer sodium,
        Integer sugar
) {

    public static MealRecordResponse from(MealRecord mealRecord, Menu menu) {
        return new MealRecordResponse(
                mealRecord.getId(),
                menu.getId(),
                menu.getName(),
                mealRecord.getMealType(),
                mealRecord.getEatenAt(),
                mealRecord.getServingAmount(),
                scale(menu.getCalories(), mealRecord.getServingAmount()),
                scale(menu.getSodium(), mealRecord.getServingAmount()),
                scale(menu.getSugar(), mealRecord.getServingAmount())
        );
    }

    private static Integer scale(Integer value, BigDecimal servingAmount) {
        return BigDecimal.valueOf(value)
                .multiply(servingAmount)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}
