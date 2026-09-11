package com.sikcourse.backend.domain.suitability.service;

import com.sikcourse.backend.domain.health.entity.HealthProfile;

public record MenuSuitabilityContext(
        HealthProfile healthProfile,
        int consumedCalories,
        int consumedSodium,
        int consumedSugar
) {
}
