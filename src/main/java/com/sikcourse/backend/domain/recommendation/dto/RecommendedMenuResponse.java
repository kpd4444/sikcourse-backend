package com.sikcourse.backend.domain.recommendation.dto;

import com.sikcourse.backend.domain.meal.entity.MenuType;
import com.sikcourse.backend.domain.suitability.dto.SuitabilityReasonResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;

import java.util.List;

public record RecommendedMenuResponse(
        Long placeId,
        String placeName,
        Long menuId,
        String menuName,
        MenuType menuType,
        Integer score,
        SuitabilityLevel level,
        List<SuitabilityReasonResponse> reasons
) {
}
