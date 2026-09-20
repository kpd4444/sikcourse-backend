package com.sikcourse.backend.domain.destination.controller;

import com.sikcourse.backend.domain.destination.dto.DestinationResolveResponse;
import com.sikcourse.backend.domain.destination.service.DestinationService;
import com.sikcourse.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Destination", description = "Destination API")
@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @Operation(summary = "Resolve destination name")
    @GetMapping("/resolve")
    public ApiResponse<DestinationResolveResponse> resolve(@RequestParam String query) {
        return ApiResponse.success(destinationService.resolve(query));
    }
}
