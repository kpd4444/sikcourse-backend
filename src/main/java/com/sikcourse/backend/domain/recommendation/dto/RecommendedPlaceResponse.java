package com.sikcourse.backend.domain.recommendation.dto;

import java.math.BigDecimal;

public record RecommendedPlaceResponse(
        Long placeId,
        String placeName,
        String addr1,
        BigDecimal mapX,
        BigDecimal mapY,
        String firstImage,
        RecommendedMenuResponse bestMenu
) {
}
