package com.sikcourse.backend.domain.meal.entity;

import com.sikcourse.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "meal_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "eaten_at", nullable = false)
    private LocalDateTime eatenAt;

    @Builder
    private MealRecord(Long userId, Long menuId, MealType mealType, LocalDateTime eatenAt) {
        this.userId = userId;
        this.menuId = menuId;
        this.mealType = mealType;
        this.eatenAt = eatenAt;
    }
}
