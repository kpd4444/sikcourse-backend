package com.sikcourse.backend.domain.health.entity;

import com.sikcourse.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(
        name = "health_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_health_profiles_user_id", columnNames = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false, length = 20)
    private ActivityLevel activityLevel;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "health_profile_diseases",
            joinColumns = @JoinColumn(name = "health_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "disease_type", nullable = false, length = 30)
    private Set<DiseaseType> diseases = new HashSet<>();

    @Column(name = "daily_calorie_goal", nullable = false)
    private Integer dailyCalorieGoal;

    @Column(name = "daily_sodium_goal", nullable = false)
    private Integer dailySodiumGoal;

    @Column(name = "daily_sugar_goal", nullable = false)
    private Integer dailySugarGoal;

    @Builder
    private HealthProfile(
            Long userId,
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
        this.userId = userId;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.activityLevel = activityLevel;
        this.diseases = new HashSet<>(diseases);
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.dailySodiumGoal = dailySodiumGoal;
        this.dailySugarGoal = dailySugarGoal;
    }

    public void update(
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
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (height != null) {
            this.height = height;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (activityLevel != null) {
            this.activityLevel = activityLevel;
        }
        if (diseases != null) {
            this.diseases = new HashSet<>(diseases);
        }
        if (dailyCalorieGoal != null) {
            this.dailyCalorieGoal = dailyCalorieGoal;
        }
        if (dailySodiumGoal != null) {
            this.dailySodiumGoal = dailySodiumGoal;
        }
        if (dailySugarGoal != null) {
            this.dailySugarGoal = dailySugarGoal;
        }
    }
}
