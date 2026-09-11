package com.sikcourse.backend.domain.health.service;

import com.sikcourse.backend.domain.health.dto.CreateHealthProfileRequest;
import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.AllergyType;
import com.sikcourse.backend.domain.health.entity.DietaryRestrictionType;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.Gender;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthProfileServiceTest {

    @Test
    void createConvertsUserIdUniqueConstraintViolationToDuplicateHealthProfile() {
        HealthProfileRepository healthProfileRepository = mock(HealthProfileRepository.class);
        HealthProfileService healthProfileService = new HealthProfileService(healthProfileRepository);
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "Duplicate health profile",
                new SQLException(),
                "uk_health_profiles_user_id"
        );

        when(healthProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(healthProfileRepository.saveAndFlush(any(HealthProfile.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation", constraintViolationException));

        assertThatThrownBy(() -> healthProfileService.create(1L, request()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(HealthErrorCode.DUPLICATE_HEALTH_PROFILE));
    }

    @Test
    void createRethrowsOtherDataIntegrityViolation() {
        HealthProfileRepository healthProfileRepository = mock(HealthProfileRepository.class);
        HealthProfileService healthProfileService = new HealthProfileService(healthProfileRepository);
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "Other constraint violation",
                new SQLException(),
                "uk_other_constraint"
        );

        when(healthProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(healthProfileRepository.saveAndFlush(any(HealthProfile.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation", constraintViolationException));

        assertThatThrownBy(() -> healthProfileService.create(1L, request()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private CreateHealthProfileRequest request() {
        return new CreateHealthProfileRequest(
                LocalDate.of(1998, 5, 20),
                Gender.MALE,
                175,
                70,
                ActivityLevel.MODERATE,
                Set.of(DiseaseType.DIABETES),
                Set.of(AllergyType.NUTS),
                Set.of(DietaryRestrictionType.LOW_SUGAR),
                2000,
                2000,
                50
        );
    }
}
