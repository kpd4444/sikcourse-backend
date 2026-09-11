package com.sikcourse.backend.domain.recommendation.controller;

import com.sikcourse.backend.domain.recommendation.dto.RecommendedMenuResponse;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedPlaceResponse;
import com.sikcourse.backend.domain.recommendation.service.RecommendationService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recommendation", description = "추천 API")
@RestController
@RequestMapping("/api/trips/{tripId}/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "여행지 기반 메뉴 추천")
    @GetMapping("/menus")
    public ApiResponse<List<RecommendedMenuResponse>> recommendMenus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long tripId
    ) {
        return ApiResponse.success(recommendationService.recommendMenus(authUser.userId(), tripId));
    }

    @Operation(summary = "여행지 기반 음식점 추천")
    @GetMapping("/places")
    public ApiResponse<List<RecommendedPlaceResponse>> recommendPlaces(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long tripId
    ) {
        return ApiResponse.success(recommendationService.recommendPlaces(authUser.userId(), tripId));
    }
}
