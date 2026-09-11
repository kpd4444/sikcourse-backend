package com.sikcourse.backend.domain.place.dto;

public record PlaceSyncResponse(
        int fetchedCount,
        int createdCount,
        int updatedCount
) {
}
