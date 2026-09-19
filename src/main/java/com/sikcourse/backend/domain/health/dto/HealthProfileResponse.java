package com.sikcourse.backend.domain.health.dto;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.Gender;
import com.sikcourse.backend.domain.health.entity.HealthProfile;

import java.time.LocalDate;
import java.util.Set;

public record HealthProfileResponse(
        Long healthProfileId,
        LocalDate birthDate,
        Gender gender,
        Integer height,
        Integer weight,
        ActivityLevel activityLevel,
        Set<DiseaseType> diseases,
        Integer dailyCalorieGoal,
        Integer dailySodiumGoal,
        Integer dailySugarGoal
) {

    public static HealthProfileResponse from(HealthProfile healthProfile) {
        return new HealthProfileResponse(
                healthProfile.getId(),
                healthProfile.getBirthDate(),
                healthProfile.getGender(),
                healthProfile.getHeight(),
                healthProfile.getWeight(),
                healthProfile.getActivityLevel(),
                Set.copyOf(healthProfile.getDiseases()),
                healthProfile.getDailyCalorieGoal(),
                healthProfile.getDailySodiumGoal(),
                healthProfile.getDailySugarGoal()
        );
    }
}
