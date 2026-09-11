package com.sikcourse.backend.domain.meal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(
        @NotBlank(message = "메뉴명은 필수입니다.")
        @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "칼로리는 필수입니다.")
        @Min(value = 0, message = "칼로리는 0 이상이어야 합니다.")
        Integer calories,

        @NotNull(message = "나트륨은 필수입니다.")
        @Min(value = 0, message = "나트륨은 0 이상이어야 합니다.")
        Integer sodium,

        @NotNull(message = "당류는 필수입니다.")
        @Min(value = 0, message = "당류는 0 이상이어야 합니다.")
        Integer sugar
) {
}
