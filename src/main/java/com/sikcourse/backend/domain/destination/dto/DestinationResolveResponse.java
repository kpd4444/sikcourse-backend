package com.sikcourse.backend.domain.destination.dto;

public record DestinationResolveResponse(
        String query,
        String areaCode,
        String areaName,
        String sigunguCode,
        String sigunguName
) {
}
