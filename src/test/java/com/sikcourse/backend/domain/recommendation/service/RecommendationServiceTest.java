package com.sikcourse.backend.domain.recommendation.service;

import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedMenuResponse;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedPlaceResponse;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;
import com.sikcourse.backend.domain.suitability.service.MenuSuitabilityService;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.domain.trip.error.TripErrorCode;
import com.sikcourse.backend.domain.trip.repository.TripRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    @Test
    void recommendMenusReturnsMenusSortedBySuitabilityScore() {
        TestContext context = testContext();
        Trip trip = trip();
        Place firstPlace = place(1L, "제주 전복집");
        Place secondPlace = place(2L, "제주 해물집");
        Menu goodMenu = menu(1L, 1L, "전복죽");
        Menu cautionMenu = menu(2L, 2L, "해물라면");

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip));
        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(firstPlace, secondPlace));
        when(context.menuRepository.findAllByPlaceIdInOrderByNameAsc(List.of(1L, 2L)))
                .thenReturn(List.of(cautionMenu, goodMenu));
        when(context.menuSuitabilityService.calculate(1L, 1L))
                .thenReturn(suitability(1L, "전복죽", 95, SuitabilityLevel.EXCELLENT));
        when(context.menuSuitabilityService.calculate(1L, 2L))
                .thenReturn(suitability(2L, "해물라면", 55, SuitabilityLevel.CAUTION));

        List<RecommendedMenuResponse> responses = context.service.recommendMenus(1L, 1L);

        assertThat(responses).extracting(RecommendedMenuResponse::menuName)
                .containsExactly("전복죽", "해물라면");
    }

    @Test
    void recommendPlacesReturnsBestMenuPerPlaceSortedByBestMenuScore() {
        TestContext context = testContext();
        Trip trip = trip();
        Place firstPlace = place(1L, "제주 전복집");
        Place secondPlace = place(2L, "제주 해물집");
        Menu bestMenu = menu(1L, 1L, "전복죽");
        Menu lowerMenu = menu(2L, 1L, "전복구이");
        Menu otherPlaceMenu = menu(3L, 2L, "해물라면");

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip));
        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(firstPlace, secondPlace));
        when(context.placeRepository.findById(1L)).thenReturn(Optional.of(firstPlace));
        when(context.placeRepository.findById(2L)).thenReturn(Optional.of(secondPlace));
        when(context.menuRepository.findAllByPlaceIdInOrderByNameAsc(List.of(1L, 2L)))
                .thenReturn(List.of(otherPlaceMenu, lowerMenu, bestMenu));
        when(context.menuSuitabilityService.calculate(1L, 1L))
                .thenReturn(suitability(1L, "전복죽", 95, SuitabilityLevel.EXCELLENT));
        when(context.menuSuitabilityService.calculate(1L, 2L))
                .thenReturn(suitability(2L, "전복구이", 80, SuitabilityLevel.GOOD));
        when(context.menuSuitabilityService.calculate(1L, 3L))
                .thenReturn(suitability(3L, "해물라면", 55, SuitabilityLevel.CAUTION));

        List<RecommendedPlaceResponse> responses = context.service.recommendPlaces(1L, 1L);

        assertThat(responses).extracting(RecommendedPlaceResponse::placeName)
                .containsExactly("제주 전복집", "제주 해물집");
        assertThat(responses.get(0).bestMenu().menuName()).isEqualTo("전복죽");
    }

    @Test
    void recommendMenusRequiresOwnedTrip() {
        TestContext context = testContext();

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> context.service.recommendMenus(1L, 1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(TripErrorCode.TRIP_NOT_FOUND));
    }

    @Test
    void recommendMenusReturnsEmptyListWhenTripHasNoPlaces() {
        TestContext context = testContext();

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip()));
        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of());

        assertThat(context.service.recommendMenus(1L, 1L)).isEmpty();
    }

    private TestContext testContext() {
        TripRepository tripRepository = mock(TripRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        MenuSuitabilityService menuSuitabilityService = mock(MenuSuitabilityService.class);
        RecommendationService service = new RecommendationService(
                tripRepository,
                placeRepository,
                menuRepository,
                menuSuitabilityService
        );

        return new TestContext(tripRepository, placeRepository, menuRepository, menuSuitabilityService, service);
    }

    private Trip trip() {
        Trip trip = Trip.builder()
                .userId(1L)
                .title("제주 여행")
                .areaCode("39")
                .sigunguCode("4")
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .baseMapX(new BigDecimal("126.5312"))
                .baseMapY(new BigDecimal("33.4996"))
                .build();
        ReflectionTestUtils.setField(trip, "id", 1L);
        return trip;
    }

    private Place place(Long id, String title) {
        Place place = Place.builder()
                .contentId("content-" + id)
                .contentTypeId("39")
                .title(title)
                .addr1("제주특별자치도")
                .addr2("")
                .areaCode("39")
                .sigunguCode("4")
                .mapX(new BigDecimal("126.1234567890"))
                .mapY(new BigDecimal("33.1234567890"))
                .tel("064-000-0000")
                .firstImage("https://example.com/image.jpg")
                .firstImage2("https://example.com/thumb.jpg")
                .cat1("A05")
                .cat2("A0502")
                .cat3("A05020100")
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private Menu menu(Long id, Long placeId, String name) {
        Menu menu = Menu.builder()
                .placeId(placeId)
                .name(name)
                .calories(400)
                .sodium(700)
                .sugar(5)
                .build();
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private MenuSuitabilityResponse suitability(
            Long menuId,
            String menuName,
            Integer score,
            SuitabilityLevel level
    ) {
        return new MenuSuitabilityResponse(menuId, menuName, score, level, List.of());
    }

    private record TestContext(
            TripRepository tripRepository,
            PlaceRepository placeRepository,
            MenuRepository menuRepository,
            MenuSuitabilityService menuSuitabilityService,
            RecommendationService service
    ) {
    }
}
