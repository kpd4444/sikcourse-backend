package com.sikcourse.backend.domain.suitability.controller;

import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.service.MenuSuitabilityService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Menu Suitability", description = "메뉴 적합도 API")
@RestController
@RequiredArgsConstructor
public class MenuSuitabilityController {

    private final MenuSuitabilityService menuSuitabilityService;

    @Operation(summary = "메뉴 적합도 조회")
    @GetMapping("/api/menus/{menuId}/suitability")
    public ApiResponse<MenuSuitabilityResponse> calculate(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long menuId
    ) {
        return ApiResponse.success(menuSuitabilityService.calculate(authUser.userId(), menuId));
    }

    @Operation(summary = "음식점 메뉴 적합도 목록 조회")
    @GetMapping("/api/places/{placeId}/menus/suitability")
    public ApiResponse<List<MenuSuitabilityResponse>> calculatePlaceMenus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long placeId
    ) {
        return ApiResponse.success(menuSuitabilityService.calculatePlaceMenus(authUser.userId(), placeId));
    }
}
