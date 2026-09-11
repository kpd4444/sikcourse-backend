package com.sikcourse.backend.domain.recommendation.dto;

import java.math.BigDecimal;

public record RecommendedWalkResponse(
        Long placeId,
        String placeName,
        String addr1,
        BigDecimal mapX,
        BigDecimal mapY,
        String firstImage
) {
}
