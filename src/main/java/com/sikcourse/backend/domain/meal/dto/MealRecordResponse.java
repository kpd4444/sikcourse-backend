package com.sikcourse.backend.domain.meal.dto;

import com.sikcourse.backend.domain.meal.entity.MealRecord;
import com.sikcourse.backend.domain.meal.entity.MealType;
import com.sikcourse.backend.domain.meal.entity.Menu;

import java.time.LocalDateTime;

public record MealRecordResponse(
        Long mealRecordId,
        Long menuId,
        String menuName,
        MealType mealType,
        LocalDateTime eatenAt,
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
                menu.getCalories(),
                menu.getSodium(),
                menu.getSugar()
        );
    }
}
