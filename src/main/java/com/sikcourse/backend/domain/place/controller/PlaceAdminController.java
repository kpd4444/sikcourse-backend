package com.sikcourse.backend.domain.place.controller;

import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.service.PlaceService;
import com.sikcourse.backend.global.config.AdminProperties;
import com.sikcourse.backend.global.error.code.GlobalErrorCode;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Tag(name = "Place Admin", description = "Place sync admin API")
@RestController
@RequestMapping("/api/admin/places")
@RequiredArgsConstructor
public class PlaceAdminController {

    private static final String ADMIN_SECRET_HEADER = "X-Admin-Secret";

    private final PlaceService placeService;
    private final AdminProperties adminProperties;

    @Operation(summary = "Sync TourAPI restaurants")
    @PostMapping("/sync/restaurants")
    public ApiResponse<PlaceSyncResponse> syncRestaurants(
            @RequestHeader(value = ADMIN_SECRET_HEADER, required = false) String adminSecret,
            @RequestParam(defaultValue = "39") String areaCode,
            @RequestParam(required = false) String sigunguCode,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer numOfRows
    ) {
        validateAdminSecret(adminSecret);
        return ApiResponse.success(placeService.syncRestaurants(areaCode, sigunguCode, pageNo, numOfRows));
    }

    @Operation(summary = "Sync TourAPI walk places")
    @PostMapping("/sync/walks")
    public ApiResponse<PlaceSyncResponse> syncWalks(
            @RequestHeader(value = ADMIN_SECRET_HEADER, required = false) String adminSecret,
            @RequestParam(defaultValue = "39") String areaCode,
            @RequestParam(required = false) String sigunguCode,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer numOfRows
    ) {
        validateAdminSecret(adminSecret);
        return ApiResponse.success(placeService.syncWalks(areaCode, sigunguCode, pageNo, numOfRows));
    }

    private void validateAdminSecret(String adminSecret) {
        String configuredSecret = adminProperties.syncSecret();
        if (isBlank(configuredSecret) || isBlank(adminSecret) || !constantTimeEquals(configuredSecret, adminSecret)) {
            throw new GeneralException(GlobalErrorCode.FORBIDDEN);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
