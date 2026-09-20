package com.sikcourse.backend.domain.meal.controller;

import com.sikcourse.backend.domain.meal.dto.CreateMealRecordRequest;
import com.sikcourse.backend.domain.meal.dto.CreateMealRecordsBatchRequest;
import com.sikcourse.backend.domain.meal.dto.CreateMealRecordsBatchResponse;
import com.sikcourse.backend.domain.meal.dto.CompleteMealRequest;
import com.sikcourse.backend.domain.meal.dto.CompleteMealResponse;
import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.meal.service.MealCompletionService;
import com.sikcourse.backend.domain.meal.service.MealRecordService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Meal Record", description = "식사 기록 API")
@RestController
@RequestMapping("/api/meal-records")
@RequiredArgsConstructor
public class MealRecordController {

    private final MealRecordService mealRecordService;
    private final MealCompletionService mealCompletionService;

    @Operation(summary = "식사 기록 등록")
    @PostMapping
    public ApiResponse<MealRecordResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateMealRecordRequest request
    ) {
        return ApiResponse.success(mealRecordService.create(authUser.userId(), request));
    }

    @Operation(summary = "식사 완료")
    @PostMapping("/complete")
    public ApiResponse<CompleteMealResponse> complete(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CompleteMealRequest request
    ) {
        return ApiResponse.success(mealCompletionService.complete(authUser.userId(), request));
    }

    @Operation(summary = "오늘 식사 기록 목록 조회")
    @GetMapping("/today")
    public ApiResponse<List<MealRecordResponse>> getTodayRecords(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(mealRecordService.getTodayRecords(authUser.userId()));
    }

    @Operation(summary = "오늘 섭취 영양 요약 조회")
    @GetMapping("/today/summary")
    public ApiResponse<DailyNutritionSummaryResponse> getTodaySummary(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(mealRecordService.getTodaySummary(authUser.userId()));
    }
}
