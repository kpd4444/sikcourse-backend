package com.sikcourse.backend.domain.place.service;

import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.message.service.GeminiMessageService;
import com.sikcourse.backend.domain.place.dto.PlaceResponse;
import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.infra.aimenu.MenuNutritionClient;
import com.sikcourse.backend.infra.aimenu.MenuNutritionMatchResponse;
import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourApiResponse;
import com.sikcourse.backend.infra.tourapi.dto.TourDetailIntroItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    @Test
    void syncRestaurantsUpdatesExistingPlaceWhenConcurrentInsertCreatesDuplicateContentId() {
        TestContext context = testContext();
        TourRestaurantItem item = restaurantItem();

        when(context.tourApiClient.areaBasedList2("39", "4", 1, 10)).thenReturn(tourApiResponse(item));
        when(context.placeUpsertService.upsert(item, PlaceType.RESTAURANT))
                .thenThrow(new DuplicatePlaceContentIdException("123456"));
        when(context.placeUpsertService.updateExisting(item, PlaceType.RESTAURANT))
                .thenReturn(PlaceUpsertResult.UPDATED);
        when(context.placeRepository.findByContentId("123456")).thenReturn(Optional.empty());

        PlaceSyncResponse response = context.service.syncRestaurants("39", "4", 1, 10);

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isZero();
        assertThat(response.updatedCount()).isEqualTo(1);
        verify(context.placeUpsertService).updateExisting(item, PlaceType.RESTAURANT);
    }

    @Test
    void syncRestaurantsCreatesMenusForSyncedPlaces() {
        TestContext context = testContext();
        TourRestaurantItem item = restaurantItem();
        Place place = place();

        when(context.tourApiClient.areaBasedList2("39", "4", 1, 10)).thenReturn(tourApiResponse(item));
        when(context.placeUpsertService.upsert(item, PlaceType.RESTAURANT)).thenReturn(PlaceUpsertResult.CREATED);
        when(context.placeRepository.findByContentId("123456")).thenReturn(Optional.of(place));
        when(context.tourApiClient.detailIntro2("123456")).thenReturn(tourIntroResponse(new TourDetailIntroItem(
                "123456",
                "39",
                "",
                "",
                "Jjamppong",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "Jajangmyeon, Cafe Latte",
                ""
        )));
        when(context.menuNutritionClient.match("Jjamppong")).thenReturn(Optional.empty());
        when(context.menuNutritionClient.match("Jajangmyeon")).thenReturn(Optional.empty());
        when(context.menuNutritionClient.match("Cafe Latte")).thenReturn(Optional.empty());

        PlaceSyncResponse response = context.service.syncRestaurants("39", "4", 1, 10);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(context.menuRepository, org.mockito.Mockito.times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Menu::getName)
                .containsExactly("Jjamppong", "Jajangmyeon", "Cafe Latte");
        assertThat(captor.getAllValues()).extracting(Menu::getMenuType)
                .containsExactly(
                        com.sikcourse.backend.domain.meal.entity.MenuType.MEAL,
                        com.sikcourse.backend.domain.meal.entity.MenuType.MEAL,
                        com.sikcourse.backend.domain.meal.entity.MenuType.DESSERT
                );
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isZero();
    }

    @Test
    void syncWalksStoresPlacesAsWalkType() {
        TestContext context = testContext();
        TourRestaurantItem item = restaurantItem();

        when(context.tourApiClient.walkAreaBasedList2("39", "4", 1, 10)).thenReturn(tourApiResponse(item));
        when(context.placeUpsertService.upsert(item, PlaceType.WALK)).thenReturn(PlaceUpsertResult.CREATED);

        PlaceSyncResponse response = context.service.syncWalks("39", "4", 1, 10);

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isZero();
        verify(context.placeUpsertService).upsert(item, PlaceType.WALK);
    }

    @Test
    void getPlacesDoesNotIncludeMenus() {
        TestContext context = testContext();
        Place place = place();

        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(place));

        List<PlaceResponse> responses = context.service.getPlaces("39", "4");

        assertThat(responses).singleElement()
                .satisfies(response -> assertThat(response.menus()).isEmpty());
    }

    @Test
    void getPlacesSyncsRestaurantsAndWalksWhenAreaHasNoPlaces() {
        TestContext context = testContext();
        TourRestaurantItem item = restaurantItem();
        Place place = place();

        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of(), List.of(place));
        when(context.tourApiClient.areaBasedList2("39", "4", 1, 100)).thenReturn(tourApiResponse(item));
        when(context.tourApiClient.walkAreaBasedList2("39", "4", 1, 100)).thenReturn(tourApiResponse(item));
        when(context.placeUpsertService.upsert(item, PlaceType.RESTAURANT)).thenReturn(PlaceUpsertResult.CREATED);
        when(context.placeUpsertService.upsert(item, PlaceType.WALK)).thenReturn(PlaceUpsertResult.CREATED);

        List<PlaceResponse> responses = context.service.getPlaces("39", "4");

        assertThat(responses).singleElement()
                .satisfies(response -> assertThat(response.placeId()).isEqualTo(1L));
        verify(context.placeUpsertService).upsert(item, PlaceType.RESTAURANT);
        verify(context.placeUpsertService).upsert(item, PlaceType.WALK);
    }

    @Test
    void getPlacesSyncsOnlyRequestedPlaceTypeWhenAreaHasNoPlaces() {
        TestContext context = testContext();
        TourRestaurantItem item = restaurantItem();
        Place place = place();

        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeAndPlaceTypeOrderByTitleAsc(
                "39",
                "4",
                PlaceType.RESTAURANT
        )).thenReturn(List.of(), List.of(place));
        when(context.tourApiClient.areaBasedList2("39", "4", 1, 100)).thenReturn(tourApiResponse(item));
        when(context.placeUpsertService.upsert(item, PlaceType.RESTAURANT)).thenReturn(PlaceUpsertResult.CREATED);

        List<PlaceResponse> responses = context.service.getPlaces("39", "4", PlaceType.RESTAURANT);

        assertThat(responses).singleElement()
                .satisfies(response -> assertThat(response.placeType()).isEqualTo(PlaceType.RESTAURANT));
        verify(context.placeUpsertService).upsert(item, PlaceType.RESTAURANT);
    }

    @Test
    void getPlacesReturnsEmptyListWhenAutoSyncFails() {
        TestContext context = testContext();

        when(context.placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc("39", "4"))
                .thenReturn(List.of());
        when(context.tourApiClient.areaBasedList2("39", "4", 1, 100))
                .thenThrow(new IllegalStateException("TourAPI unavailable"));
        when(context.tourApiClient.walkAreaBasedList2("39", "4", 1, 100))
                .thenThrow(new IllegalStateException("TourAPI unavailable"));

        assertThat(context.service.getPlaces("39", "4")).isEmpty();
    }

    @Test
    void getPlaceCreatesMenusFromTourApiIntroWithAiNutrition() {
        TestContext context = testContext();
        Place place = place();

        when(context.placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(context.geminiMessageService.placeRecommendationPoint(place)).thenReturn("Good place");
        when(context.tourApiClient.detailIntro2("123456")).thenReturn(tourIntroResponse(new TourDetailIntroItem(
                "123456",
                "39",
                "",
                "",
                "Jjamppong",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "Jajangmyeon, Tangsuyuk",
                ""
        )));
        when(context.menuNutritionClient.match("Jjamppong")).thenReturn(Optional.of(new MenuNutritionMatchResponse(
                "Jjamppong",
                86.0,
                195.0,
                0.96,
                0.29,
                1.0,
                false
        )));
        when(context.menuNutritionClient.match("Jajangmyeon")).thenReturn(Optional.empty());
        when(context.menuNutritionClient.match("Tangsuyuk")).thenReturn(Optional.empty());
        when(context.menuRepository.findAllByPlaceIdOrderByNameAsc(1L)).thenReturn(List.of(
                Menu.builder()
                        .placeId(1L)
                        .name("Jjamppong")
                        .calories(86)
                        .sodium(195)
                        .sugar(1)
                        .build()
        ));

        PlaceResponse response = context.service.getPlace(1L);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(context.menuRepository, org.mockito.Mockito.times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Menu::getName)
                .containsExactly("Jjamppong", "Jajangmyeon", "Tangsuyuk");
        Menu jjamppong = captor.getAllValues().get(0);
        assertThat(jjamppong.getCalories()).isEqualTo(86);
        assertThat(jjamppong.getSodium()).isEqualTo(195);
        assertThat(jjamppong.getSugar()).isEqualTo(1);
        assertThat(response.menus()).singleElement()
                .satisfies(menu -> assertThat(menu.name()).isEqualTo("Jjamppong"));
    }

    @Test
    void getPlaceEnrichesExistingEmptyMenuFromTourApiIntro() {
        TestContext context = testContext();
        Place place = place();
        Menu menu = Menu.builder()
                .placeId(1L)
                .name("Jjamppong")
                .calories(0)
                .sodium(0)
                .sugar(0)
                .build();

        when(context.placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(context.geminiMessageService.placeRecommendationPoint(place)).thenReturn("Good place");
        when(context.tourApiClient.detailIntro2("123456")).thenReturn(tourIntroResponse(new TourDetailIntroItem(
                "123456",
                "39",
                "",
                "",
                "Jjamppong",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        )));
        when(context.menuRepository.findByPlaceIdAndNameIgnoreCase(1L, "Jjamppong"))
                .thenReturn(Optional.of(menu));
        when(context.menuNutritionClient.match("Jjamppong")).thenReturn(Optional.of(new MenuNutritionMatchResponse(
                "Jjamppong",
                86.0,
                195.0,
                0.96,
                0.29,
                1.0,
                false
        )));
        when(context.menuRepository.findAllByPlaceIdOrderByNameAsc(1L)).thenReturn(List.of(menu));

        PlaceResponse response = context.service.getPlace(1L);

        verify(context.menuRepository).save(menu);
        assertThat(response.menus()).singleElement()
                .satisfies(savedMenu -> {
                    assertThat(savedMenu.calories()).isEqualTo(86);
                    assertThat(savedMenu.sodium()).isEqualTo(195);
                    assertThat(savedMenu.sugar()).isEqualTo(1);
                });
    }

    private TestContext testContext() {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        TourApiClient tourApiClient = mock(TourApiClient.class);
        PlaceUpsertService placeUpsertService = mock(PlaceUpsertService.class);
        GeminiMessageService geminiMessageService = mock(GeminiMessageService.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        PlaceService service = new PlaceService(
                placeRepository,
                menuRepository,
                tourApiClient,
                placeUpsertService,
                geminiMessageService,
                menuNutritionClient
        );

        return new TestContext(
                placeRepository,
                menuRepository,
                tourApiClient,
                placeUpsertService,
                geminiMessageService,
                menuNutritionClient,
                service
        );
    }

    private Place place() {
        Place place = Place.builder()
                .contentId("123456")
                .contentTypeId("39")
                .placeType(PlaceType.RESTAURANT)
                .title("Jeju Restaurant")
                .areaCode("39")
                .sigunguCode("4")
                .mapX(new BigDecimal("126.1234567890"))
                .mapY(new BigDecimal("33.1234567890"))
                .build();
        ReflectionTestUtils.setField(place, "id", 1L);
        return place;
    }

    private TourApiResponse<TourRestaurantItem> tourApiResponse(TourRestaurantItem item) {
        return new TourApiResponse<>(
                new TourApiResponse.Header("0000", "OK"),
                new TourApiResponse.Body<>(10, 1, 1, List.of(item))
        );
    }

    private TourApiResponse<TourDetailIntroItem> tourIntroResponse(TourDetailIntroItem item) {
        return new TourApiResponse<>(
                new TourApiResponse.Header("0000", "OK"),
                new TourApiResponse.Body<>(10, 1, 1, List.of(item))
        );
    }

    private TourRestaurantItem restaurantItem() {
        return new TourRestaurantItem(
                "Jeju addr1",
                "",
                "39",
                "",
                "A05",
                "A0502",
                "A05020100",
                "123456",
                "39",
                "20260911120000",
                "https://example.com/image.jpg",
                "https://example.com/thumb.jpg",
                "",
                "126.1234567890",
                "33.1234567890",
                "6",
                "20260911120000",
                "4",
                "064-000-0000",
                "Jeju Restaurant",
                "63000"
        );
    }

    private record TestContext(
            PlaceRepository placeRepository,
            MenuRepository menuRepository,
            TourApiClient tourApiClient,
            PlaceUpsertService placeUpsertService,
            GeminiMessageService geminiMessageService,
            MenuNutritionClient menuNutritionClient,
            PlaceService service
    ) {
    }
}
