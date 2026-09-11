package com.sikcourse.backend.domain.recommendation.service;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.Gender;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedMenuResponse;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedPlaceResponse;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;
import com.sikcourse.backend.domain.suitability.service.MenuSuitabilityContext;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    @Test
    void recommendMenusReturnsMenusSortedBySuitabilityScore() {
        TestContext context = testContext();
        Trip trip = trip();
        Place firstPlace = place(1L, "Jeju Abalone");
        Place secondPlace = place(2L, "Jeju Seafood");
        Menu goodMenu = menu(1L, 1L, "Abalone Porridge");
        Menu cautionMenu = menu(2L, 2L, "Seafood Ramen");
        MenuSuitabilityContext suitabilityContext = suitabilityContext();

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip));
        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(firstPlace, secondPlace));
        when(context.menuRepository.findAllByPlaceIdInOrderByNameAsc(List.of(1L, 2L)))
                .thenReturn(List.of(cautionMenu, goodMenu));
        when(context.menuSuitabilityService.createContext(1L)).thenReturn(suitabilityContext);
        when(context.menuSuitabilityService.calculate(suitabilityContext, goodMenu))
                .thenReturn(suitability(1L, "Abalone Porridge", 95, SuitabilityLevel.EXCELLENT));
        when(context.menuSuitabilityService.calculate(suitabilityContext, cautionMenu))
                .thenReturn(suitability(2L, "Seafood Ramen", 55, SuitabilityLevel.CAUTION));

        List<RecommendedMenuResponse> responses = context.service.recommendMenus(1L, 1L);

        assertThat(responses).extracting(RecommendedMenuResponse::menuName)
                .containsExactly("Abalone Porridge", "Seafood Ramen");
        verify(context.menuSuitabilityService).createContext(1L);
    }

    @Test
    void recommendPlacesReturnsBestMenuPerPlaceSortedByBestMenuScore() {
        TestContext context = testContext();
        Trip trip = trip();
        Place firstPlace = place(1L, "Jeju Abalone");
        Place secondPlace = place(2L, "Jeju Seafood");
        Menu bestMenu = menu(1L, 1L, "Abalone Porridge");
        Menu lowerMenu = menu(2L, 1L, "Grilled Abalone");
        Menu otherPlaceMenu = menu(3L, 2L, "Seafood Ramen");
        MenuSuitabilityContext suitabilityContext = suitabilityContext();

        when(context.tripRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(trip));
        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(firstPlace, secondPlace));
        when(context.placeRepository.findById(1L)).thenReturn(Optional.of(firstPlace));
        when(context.placeRepository.findById(2L)).thenReturn(Optional.of(secondPlace));
        when(context.menuRepository.findAllByPlaceIdInOrderByNameAsc(List.of(1L, 2L)))
                .thenReturn(List.of(otherPlaceMenu, lowerMenu, bestMenu));
        when(context.menuSuitabilityService.createContext(1L)).thenReturn(suitabilityContext);
        when(context.menuSuitabilityService.calculate(suitabilityContext, bestMenu))
                .thenReturn(suitability(1L, "Abalone Porridge", 95, SuitabilityLevel.EXCELLENT));
        when(context.menuSuitabilityService.calculate(suitabilityContext, lowerMenu))
                .thenReturn(suitability(2L, "Grilled Abalone", 80, SuitabilityLevel.GOOD));
        when(context.menuSuitabilityService.calculate(suitabilityContext, otherPlaceMenu))
                .thenReturn(suitability(3L, "Seafood Ramen", 55, SuitabilityLevel.CAUTION));

        List<RecommendedPlaceResponse> responses = context.service.recommendPlaces(1L, 1L);

        assertThat(responses).extracting(RecommendedPlaceResponse::placeName)
                .containsExactly("Jeju Abalone", "Jeju Seafood");
        assertThat(responses.get(0).bestMenu().menuName()).isEqualTo("Abalone Porridge");
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

    private MenuSuitabilityContext suitabilityContext() {
        return new MenuSuitabilityContext(healthProfile(), 0, 0, 0);
    }

    private HealthProfile healthProfile() {
        return HealthProfile.builder()
                .userId(1L)
                .birthDate(LocalDate.of(1998, 5, 20))
                .gender(Gender.MALE)
                .height(175)
                .weight(70)
                .activityLevel(ActivityLevel.MODERATE)
                .diseases(Set.of())
                .allergies(Set.of())
                .dietaryRestrictions(Set.of())
                .dailyCalorieGoal(2000)
                .dailySodiumGoal(2000)
                .dailySugarGoal(50)
                .build();
    }

    private Trip trip() {
        Trip trip = Trip.builder()
                .userId(1L)
                .title("Jeju Trip")
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
                .addr1("Jeju")
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
