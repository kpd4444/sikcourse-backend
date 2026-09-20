package com.sikcourse.backend.domain.destination.service;

import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourApiResponse;
import com.sikcourse.backend.infra.tourapi.dto.TourAreaCodeItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DestinationServiceTest {

    @Test
    void resolveReturnsAreaAndSigunguCodesFromDestinationName() {
        TourApiClient tourApiClient = mock(TourApiClient.class);
        DestinationService destinationService = new DestinationService(tourApiClient);

        when(tourApiClient.areaCode2()).thenReturn(response(List.of(
                new TourAreaCodeItem("1", "서울"),
                new TourAreaCodeItem("39", "제주특별자치도")
        )));
        when(tourApiClient.areaCode2("39")).thenReturn(response(List.of(
                new TourAreaCodeItem("3", "서귀포시"),
                new TourAreaCodeItem("4", "제주시")
        )));

        var response = destinationService.resolve("제주특별자치도 서귀포시 성산읍");

        assertThat(response.areaCode()).isEqualTo("39");
        assertThat(response.areaName()).isEqualTo("제주특별자치도");
        assertThat(response.sigunguCode()).isEqualTo("3");
        assertThat(response.sigunguName()).isEqualTo("서귀포시");
    }

    @Test
    void resolveFindsSigunguWhenAreaNameIsMissing() {
        TourApiClient tourApiClient = mock(TourApiClient.class);
        DestinationService destinationService = new DestinationService(tourApiClient);

        when(tourApiClient.areaCode2()).thenReturn(response(List.of(
                new TourAreaCodeItem("1", "서울"),
                new TourAreaCodeItem("37", "전북특별자치도")
        )));
        when(tourApiClient.areaCode2("1")).thenReturn(response(List.of(
                new TourAreaCodeItem("1", "종로구")
        )));
        when(tourApiClient.areaCode2("37")).thenReturn(response(List.of(
                new TourAreaCodeItem("12", "전주시")
        )));

        var response = destinationService.resolve("전주 한옥마을");

        assertThat(response.areaCode()).isEqualTo("37");
        assertThat(response.areaName()).isEqualTo("전북특별자치도");
        assertThat(response.sigunguCode()).isEqualTo("12");
        assertThat(response.sigunguName()).isEqualTo("전주시");
    }

    @Test
    void resolveFindsSigunguFromPlaceKeywordWhenOnlyAreaNameMatches() {
        TourApiClient tourApiClient = mock(TourApiClient.class);
        DestinationService destinationService = new DestinationService(tourApiClient);

        when(tourApiClient.areaCode2()).thenReturn(response(List.of(
                new TourAreaCodeItem("3", "대전")
        )));
        when(tourApiClient.areaCode2("3")).thenReturn(response(List.of(
                new TourAreaCodeItem("5", "중구")
        )));
        when(tourApiClient.searchKeyword2("성심당", 1, 10)).thenReturn(placeResponse(List.of(
                restaurant("3", "5", "성심당")
        )));

        var response = destinationService.resolve("대전 성심당");

        assertThat(response.areaCode()).isEqualTo("3");
        assertThat(response.areaName()).isEqualTo("대전");
        assertThat(response.sigunguCode()).isEqualTo("5");
        assertThat(response.sigunguName()).isEqualTo("중구");
    }

    private TourApiResponse<TourAreaCodeItem> response(List<TourAreaCodeItem> items) {
        return new TourApiResponse<>(
                new TourApiResponse.Header("0000", "OK"),
                new TourApiResponse.Body<>(100, 1, items.size(), items)
        );
    }

    private TourApiResponse<TourRestaurantItem> placeResponse(List<TourRestaurantItem> items) {
        return new TourApiResponse<>(
                new TourApiResponse.Header("0000", "OK"),
                new TourApiResponse.Body<>(10, 1, items.size(), items)
        );
    }

    private TourRestaurantItem restaurant(String areaCode, String sigunguCode, String title) {
        return new TourRestaurantItem(
                null,
                null,
                areaCode,
                null,
                null,
                null,
                null,
                "1",
                "39",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                sigunguCode,
                null,
                title,
                null
        );
    }
}
