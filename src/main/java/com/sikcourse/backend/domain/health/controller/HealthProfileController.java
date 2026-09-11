package com.sikcourse.backend.domain.health.controller;

import com.sikcourse.backend.domain.health.dto.CreateHealthProfileRequest;
import com.sikcourse.backend.domain.health.dto.HealthProfileResponse;
import com.sikcourse.backend.domain.health.dto.UpdateHealthProfileRequest;
import com.sikcourse.backend.domain.health.service.HealthProfileService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Profile", description = "건강 프로필")
@RestController
@RequestMapping("/api/health-profiles")
@RequiredArgsConstructor
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @Operation(summary = "건강 프로필 등록")
    @PostMapping
    public ApiResponse<HealthProfileResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateHealthProfileRequest request
    ) {
        return ApiResponse.success(healthProfileService.create(authUser.userId(), request));
    }

    @Operation(summary = "내 건강 프로필 조회")
    @GetMapping("/me")
    public ApiResponse<HealthProfileResponse> getMine(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(healthProfileService.getMine(authUser.userId()));
    }

    @Operation(summary = "건강 프로필 수정")
    @PatchMapping("/me")
    public ApiResponse<HealthProfileResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UpdateHealthProfileRequest request
    ) {
        return ApiResponse.success(healthProfileService.update(authUser.userId(), request));
    }
}
