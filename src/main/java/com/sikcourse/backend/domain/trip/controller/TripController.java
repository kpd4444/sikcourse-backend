package com.sikcourse.backend.domain.trip.controller;

import com.sikcourse.backend.domain.trip.dto.CreateTripRequest;
import com.sikcourse.backend.domain.trip.dto.TripResponse;
import com.sikcourse.backend.domain.trip.dto.UpdateTripRequest;
import com.sikcourse.backend.domain.trip.service.TripService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Trip", description = "Trip API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @Operation(summary = "Create trip")
    @PostMapping
    public ApiResponse<TripResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateTripRequest request
    ) {
        return ApiResponse.success(tripService.create(authUser.userId(), request));
    }

    @Operation(summary = "Get my trips")
    @GetMapping
    public ApiResponse<List<TripResponse>> getTrips(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(tripService.getTrips(authUser.userId()));
    }

    @Operation(summary = "Get my trip")
    @GetMapping("/{tripId}")
    public ApiResponse<TripResponse> getTrip(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long tripId
    ) {
        return ApiResponse.success(tripService.getTrip(authUser.userId(), tripId));
    }

    @Operation(summary = "Update my trip")
    @PatchMapping("/{tripId}")
    public ApiResponse<TripResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        return ApiResponse.success(tripService.update(authUser.userId(), tripId, request));
    }

    @Operation(summary = "Delete my trip")
    @DeleteMapping("/{tripId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long tripId
    ) {
        tripService.delete(authUser.userId(), tripId);
        return ApiResponse.success();
    }
}
