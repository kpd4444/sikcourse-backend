package com.sikcourse.backend.domain.place.controller;

import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.service.PlaceService;
import com.sikcourse.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place Dev", description = "Place sync dev API")
@RestController
@RequestMapping("/api/dev/places")
@RequiredArgsConstructor
public class PlaceDevController {

    private final PlaceService placeService;

    @Operation(summary = "Sync TourAPI restaurants")
    @PostMapping("/sync/restaurants")
    public ApiResponse<PlaceSyncResponse> syncRestaurants(
            @RequestParam(defaultValue = "39") String areaCode,
            @RequestParam(required = false) String sigunguCode,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer numOfRows
    ) {
        return ApiResponse.success(placeService.syncRestaurants(areaCode, sigunguCode, pageNo, numOfRows));
    }
}
