package com.sikcourse.backend.infra.tourapi;

import com.sikcourse.backend.infra.tourapi.dto.TourApiResponse;
import com.sikcourse.backend.infra.tourapi.dto.TourDetailIntroItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개발 확인용 임시 API. 정식 기능 구현 후 삭제.
 */
@Tag(name = "TourAPI Dev", description = "TourAPI 연동 개발 확인용")
@Profile("local")
@RestController
@RequiredArgsConstructor
public class TourApiDevController {

    private final TourApiClient tourApiClient;

    @Operation(summary = "지역 기반 음식점 목록 조회")
    @GetMapping("/api/dev/tour/restaurants")
    public TourApiResponse<TourRestaurantItem> restaurants(
            @RequestParam(defaultValue = "39") String areaCode,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer numOfRows
    ) {
        return tourApiClient.areaBasedList2(areaCode, pageNo, numOfRows);
    }

    @Operation(summary = "음식점 소개 상세 조회")
    @GetMapping("/api/dev/tour/intro")
    public TourApiResponse<TourDetailIntroItem> intro(@RequestParam String contentId) {
        return tourApiClient.detailIntro2(contentId);
    }
}
