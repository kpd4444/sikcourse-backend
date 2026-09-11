package com.sikcourse.backend.domain.meal.dto;

import com.sikcourse.backend.domain.meal.entity.MealType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CompleteMealRequest(
        @NotNull(message = "여행 ID는 필수입니다.")
        Long tripId,

        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @NotNull(message = "식사 유형은 필수입니다.")
        MealType mealType,

        @NotNull(message = "식사 시간은 필수입니다.")
        LocalDateTime eatenAt
) {

    public CreateMealRecordRequest toMealRecordRequest() {
        return new CreateMealRecordRequest(menuId, mealType, eatenAt);
    }
}
