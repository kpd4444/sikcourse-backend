package com.sikcourse.backend.domain.health.service;

import com.sikcourse.backend.domain.health.dto.RecommendedHealthGoalsResponse;
import com.sikcourse.backend.domain.health.entity.BmiStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendedHealthGoalsServiceTest {

    private final RecommendedHealthGoalsService service = new RecommendedHealthGoalsService();

    @Test
    void calculateReturnsDefaultGoalsAndRoundedBmi() {
        RecommendedHealthGoalsResponse response = service.calculate(175, 70);

        assertThat(response.dailyCalorieGoal()).isEqualTo(1800);
        assertThat(response.dailySodiumGoal()).isEqualTo(1500);
        assertThat(response.dailySugarGoal()).isEqualTo(25);
        assertThat(response.bmi()).isEqualTo(22.9);
        assertThat(response.bmiStatus()).isEqualTo(BmiStatus.NORMAL);
    }

    @Test
    void calculateClassifiesBmiStatus() {
        assertThat(service.calculate(180, 55).bmiStatus()).isEqualTo(BmiStatus.UNDERWEIGHT);
        assertThat(service.calculate(175, 70).bmiStatus()).isEqualTo(BmiStatus.NORMAL);
        assertThat(service.calculate(170, 68).bmiStatus()).isEqualTo(BmiStatus.OVERWEIGHT);
        assertThat(service.calculate(170, 80).bmiStatus()).isEqualTo(BmiStatus.OBESE);
    }
}
