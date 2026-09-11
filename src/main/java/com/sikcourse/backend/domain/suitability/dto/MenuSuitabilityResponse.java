package com.sikcourse.backend.domain.suitability.dto;

import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;

import java.util.List;

public record MenuSuitabilityResponse(
        Long menuId,
        String menuName,
        Integer score,
        SuitabilityLevel level,
        List<SuitabilityReasonResponse> reasons
) {
}
