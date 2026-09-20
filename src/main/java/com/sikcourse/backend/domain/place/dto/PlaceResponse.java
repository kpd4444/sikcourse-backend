package com.sikcourse.backend.domain.place.dto;

import com.sikcourse.backend.domain.meal.dto.MenuResponse;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;

import java.math.BigDecimal;
import java.util.List;

public record PlaceResponse(
        Long placeId,
        String contentId,
        String contentTypeId,
        PlaceType placeType,
        String title,
        String addr1,
        String addr2,
        String areaCode,
        String sigunguCode,
        BigDecimal mapX,
        BigDecimal mapY,
        String tel,
        String firstImage,
        String firstImage2,
        String cat1,
        String cat2,
        String cat3,
        String aiRecommendationPoint,
        List<MenuResponse> menus
) {

    public static PlaceResponse from(Place place) {
        return from(place, null, List.of());
    }

    public static PlaceResponse from(Place place, String aiRecommendationPoint) {
        return from(place, aiRecommendationPoint, List.of());
    }

    public static PlaceResponse from(Place place, List<MenuResponse> menus) {
        return from(place, null, menus);
    }

    public static PlaceResponse from(Place place, String aiRecommendationPoint, List<MenuResponse> menus) {
        return new PlaceResponse(
                place.getId(),
                place.getContentId(),
                place.getContentTypeId(),
                place.getPlaceType(),
                place.getTitle(),
                place.getAddr1(),
                place.getAddr2(),
                place.getAreaCode(),
                place.getSigunguCode(),
                place.getMapX(),
                place.getMapY(),
                place.getTel(),
                place.getFirstImage(),
                place.getFirstImage2(),
                place.getCat1(),
                place.getCat2(),
                place.getCat3(),
                aiRecommendationPoint,
                menus
        );
    }
}
