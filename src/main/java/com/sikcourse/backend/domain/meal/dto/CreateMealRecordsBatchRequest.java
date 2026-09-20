package com.sikcourse.backend.domain.meal.dto;

import com.sikcourse.backend.domain.meal.entity.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreateMealRecordsBatchRequest(
        @NotNull(message = "식사 유형은 필수입니다.")
        MealType mealType,

        @NotNull(message = "식사 시간은 필수입니다.")
        LocalDateTime eatenAt,

        @Valid
        @NotEmpty(message = "식사 기록 항목은 1개 이상이어야 합니다.")
        List<CreateMealRecordItemRequest> items
) {
}
