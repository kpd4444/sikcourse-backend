package com.sikcourse.backend.domain.health.service;

import com.sikcourse.backend.domain.health.dto.RecommendedHealthGoalsResponse;
import com.sikcourse.backend.domain.health.entity.BmiStatus;
import org.springframework.stereotype.Service;

@Service
public class RecommendedHealthGoalsService {

    private static final int DAILY_CALORIE_GOAL = 1800;
    private static final int DAILY_SODIUM_GOAL = 1500;
    private static final int DAILY_SUGAR_GOAL = 25;

    public RecommendedHealthGoalsResponse calculate(Integer height, Integer weight) {
        double bmi = calculateBmi(height, weight);
        return new RecommendedHealthGoalsResponse(
                DAILY_CALORIE_GOAL,
                DAILY_SODIUM_GOAL,
                DAILY_SUGAR_GOAL,
                bmi,
                bmiStatus(bmi)
        );
    }

    private double calculateBmi(Integer height, Integer weight) {
        double heightMeters = height / 100.0;
        double bmi = weight / (heightMeters * heightMeters);
        return Math.round(bmi * 10) / 10.0;
    }

    private BmiStatus bmiStatus(double bmi) {
        if (bmi < 18.5) {
            return BmiStatus.UNDERWEIGHT;
        }
        if (bmi < 23) {
            return BmiStatus.NORMAL;
        }
        if (bmi < 25) {
            return BmiStatus.OVERWEIGHT;
        }
        return BmiStatus.OBESE;
    }
}
