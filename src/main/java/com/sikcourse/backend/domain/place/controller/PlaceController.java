package com.sikcourse.backend.domain.place.controller;

import com.sikcourse.backend.domain.place.dto.PlaceResponse;
import com.sikcourse.backend.domain.place.service.PlaceService;
import com.sikcourse.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Place", description = "Place API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "Get places")
    @GetMapping
    public ApiResponse<List<PlaceResponse>> getPlaces(
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String sigunguCode
    ) {
        return ApiResponse.success(placeService.getPlaces(areaCode, sigunguCode));
    }

    @Operation(summary = "Get place")
    @GetMapping("/{placeId}")
    public ApiResponse<PlaceResponse> getPlace(@PathVariable Long placeId) {
        return ApiResponse.success(placeService.getPlace(placeId));
    }
}
