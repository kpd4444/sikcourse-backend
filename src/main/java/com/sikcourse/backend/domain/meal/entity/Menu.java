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

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type", nullable = false, length = 20)
    private MenuType menuType;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Integer sodium;

    @Column(nullable = false)
    private Integer sugar;

    @Builder
    private Menu(Long placeId, String name, MenuType menuType, Integer calories, Integer sodium, Integer sugar) {
        this.placeId = placeId;
        this.name = name;
        this.menuType = menuType == null ? MenuType.MEAL : menuType;
        this.calories = calories;
        this.sodium = sodium;
        this.sugar = sugar;
    }

    public void updateNutrition(Integer calories, Integer sodium, Integer sugar) {
        this.calories = calories;
        this.sodium = sodium;
        this.sugar = sugar;
    }

    public void updateMenuType(MenuType menuType) {
        this.menuType = menuType == null ? MenuType.MEAL : menuType;
    }
}
