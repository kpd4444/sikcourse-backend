package com.sikcourse.backend.domain.suitability.dto;

import com.sikcourse.backend.domain.suitability.entity.SuitabilityReasonType;

public record SuitabilityReasonResponse(
        SuitabilityReasonType type,
        String message,
        Integer penalty,
        Integer exceededAmount
) {
}
