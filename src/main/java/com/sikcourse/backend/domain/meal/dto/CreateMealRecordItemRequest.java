package com.sikcourse.backend.domain.meal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMealRecordItemRequest(
        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @NotNull(message = "인분 수는 필수입니다.")
        @DecimalMin(value = "0.01", message = "인분 수는 0.01 이상이어야 합니다.")
        BigDecimal servingAmount
) {
}
