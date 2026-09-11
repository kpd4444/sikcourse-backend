package com.sikcourse.backend.domain.health.dto;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.AllergyType;
import com.sikcourse.backend.domain.health.entity.DietaryRestrictionType;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record CreateHealthProfileRequest(
        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotNull(message = "키는 필수입니다.")
        @Min(value = 1, message = "키는 1 이상이어야 합니다.")
        Integer height,

        @NotNull(message = "몸무게는 필수입니다.")
        @Min(value = 1, message = "몸무게는 1 이상이어야 합니다.")
        Integer weight,

        @NotNull(message = "운동 빈도는 필수입니다.")
        ActivityLevel activityLevel,

        @NotNull(message = "보유 질병 목록은 필수입니다.")
        Set<DiseaseType> diseases,

        @NotNull(message = "알레르기 목록은 필수입니다.")
        Set<AllergyType> allergies,

        @NotNull(message = "식이제한 목록은 필수입니다.")
        Set<DietaryRestrictionType> dietaryRestrictions,

        @NotNull(message = "일일 목표 칼로리는 필수입니다.")
        @Min(value = 1, message = "일일 목표 칼로리는 1 이상이어야 합니다.")
        Integer dailyCalorieGoal,

        @NotNull(message = "일일 목표 나트륨은 필수입니다.")
        @Min(value = 1, message = "일일 목표 나트륨은 1 이상이어야 합니다.")
        Integer dailySodiumGoal,

        @NotNull(message = "일일 목표 당류는 필수입니다.")
        @Min(value = 1, message = "일일 목표 당류는 1 이상이어야 합니다.")
        Integer dailySugarGoal
) {
}
