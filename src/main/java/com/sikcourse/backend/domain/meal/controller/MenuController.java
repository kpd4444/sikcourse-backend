package com.sikcourse.backend.domain.meal.controller;

import com.sikcourse.backend.domain.meal.dto.CreateMenuRequest;
import com.sikcourse.backend.domain.meal.dto.MenuResponse;
import com.sikcourse.backend.domain.meal.service.MenuService;
import com.sikcourse.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Menu", description = "메뉴 API")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "음식점 메뉴 등록")
    @PostMapping("/api/places/{placeId}/menus")
    public ApiResponse<MenuResponse> create(
            @PathVariable Long placeId,
            @Valid @RequestBody CreateMenuRequest request
    ) {
        return ApiResponse.success(menuService.create(placeId, request));
    }

    @Operation(summary = "음식점 메뉴 목록 조회")
    @GetMapping("/api/places/{placeId}/menus")
    public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long placeId) {
        return ApiResponse.success(menuService.getMenus(placeId));
    }

    @Operation(summary = "메뉴 상세 조회")
    @GetMapping("/api/menus/{menuId}")
    public ApiResponse<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ApiResponse.success(menuService.getMenu(menuId));
    }
}
