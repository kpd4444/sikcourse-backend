package com.sikcourse.backend.domain.health.service;

import com.sikcourse.backend.domain.health.dto.CreateHealthProfileRequest;
import com.sikcourse.backend.domain.health.dto.HealthProfileResponse;
import com.sikcourse.backend.domain.health.dto.UpdateHealthProfileRequest;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthProfileService {

    private final HealthProfileRepository healthProfileRepository;

    @Transactional
    public HealthProfileResponse create(Long userId, CreateHealthProfileRequest request) {
        if (healthProfileRepository.existsByUserId(userId)) {
            throw new GeneralException(HealthErrorCode.DUPLICATE_HEALTH_PROFILE);
        }

        HealthProfile healthProfile = HealthProfile.builder()
                .userId(userId)
                .birthDate(request.birthDate())
                .gender(request.gender())
                .height(request.height())
                .weight(request.weight())
                .activityLevel(request.activityLevel())
                .diseases(request.diseases())
                .dailyCalorieGoal(request.dailyCalorieGoal())
                .dailySodiumGoal(request.dailySodiumGoal())
                .dailySugarGoal(request.dailySugarGoal())
                .build();

        try {
            return HealthProfileResponse.from(healthProfileRepository.saveAndFlush(healthProfile));
        } catch (DataIntegrityViolationException exception) {
            if (isUserIdUniqueConstraintViolation(exception)) {
                throw new GeneralException(HealthErrorCode.DUPLICATE_HEALTH_PROFILE);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public HealthProfileResponse getMine(Long userId) {
        HealthProfile healthProfile = getByUserId(userId);
        return HealthProfileResponse.from(healthProfile);
    }

    @Transactional
    public HealthProfileResponse update(Long userId, UpdateHealthProfileRequest request) {
        HealthProfile healthProfile = getByUserId(userId);
        healthProfile.update(
                request.birthDate(),
                request.gender(),
                request.height(),
                request.weight(),
                request.activityLevel(),
                request.diseases(),
                request.dailyCalorieGoal(),
                request.dailySodiumGoal(),
                request.dailySugarGoal()
        );
        return HealthProfileResponse.from(healthProfile);
    }

    private HealthProfile getByUserId(Long userId) {
        return healthProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(HealthErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }

    private boolean isUserIdUniqueConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                return "uk_health_profiles_user_id".equalsIgnoreCase(
                        constraintViolationException.getConstraintName()
                );
            }
            current = current.getCause();
        }
        return false;
    }
}
