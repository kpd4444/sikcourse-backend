package com.sikcourse.backend.domain.meal.dto;

import com.sikcourse.backend.domain.meal.entity.Menu;

public record MenuResponse(
        Long menuId,
        Long placeId,
        String name,
        Integer calories,
        Integer sodium,
        Integer sugar
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getPlaceId(),
                menu.getName(),
                menu.getCalories(),
                menu.getSodium(),
                menu.getSugar()
        );
    }
}
