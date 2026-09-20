package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.meal.dto.CreateMenuRequest;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.entity.MenuType;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.infra.aimenu.MenuNutritionClient;
import com.sikcourse.backend.infra.aimenu.MenuNutritionMatchResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuServiceTest {

    @Test
    void createUsesAiNutritionWhenAvailable() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        MenuService menuService = new MenuService(menuRepository, placeRepository, menuNutritionClient);
        CreateMenuRequest request = new CreateMenuRequest("참치김밥", MenuType.MEAL, 300, 500, 5);

        when(placeRepository.existsById(1L)).thenReturn(true);
        when(menuNutritionClient.match("참치김밥")).thenReturn(Optional.of(new MenuNutritionMatchResponse(
                "김밥_참치",
                128.4,
                175.2,
                0.57,
                0.64,
                0.905,
                false
        )));
        when(menuRepository.save(org.mockito.ArgumentMatchers.any(Menu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        menuService.create(1L, request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        Menu savedMenu = captor.getValue();
        assertThat(savedMenu.getCalories()).isEqualTo(128);
        assertThat(savedMenu.getSodium()).isEqualTo(175);
        assertThat(savedMenu.getSugar()).isEqualTo(1);
    }

    @Test
    void createKeepsRequestNutritionWhenAiReturnsNullValues() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        MenuService menuService = new MenuService(menuRepository, placeRepository, menuNutritionClient);
        CreateMenuRequest request = new CreateMenuRequest("제로 사이다", MenuType.DESSERT, 10, 20, 0);

        when(placeRepository.existsById(1L)).thenReturn(true);
        when(menuNutritionClient.match("제로 사이다")).thenReturn(Optional.of(new MenuNutritionMatchResponse(
                "제로 사이다",
                null,
                null,
                null,
                null,
                0.3,
                true
        )));
        when(menuRepository.save(org.mockito.ArgumentMatchers.any(Menu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        menuService.create(1L, request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        Menu savedMenu = captor.getValue();
        assertThat(savedMenu.getCalories()).isEqualTo(10);
        assertThat(savedMenu.getSodium()).isEqualTo(20);
        assertThat(savedMenu.getSugar()).isEqualTo(0);
    }

    @Test
    void createKeepsRequestNutritionWhenAiFails() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        MenuService menuService = new MenuService(menuRepository, placeRepository, menuNutritionClient);
        CreateMenuRequest request = new CreateMenuRequest("전복죽", MenuType.MEAL, 850, 650, 18);

        when(placeRepository.existsById(1L)).thenReturn(true);
        when(menuNutritionClient.match("전복죽")).thenReturn(Optional.empty());
        when(menuRepository.save(org.mockito.ArgumentMatchers.any(Menu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        menuService.create(1L, request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        Menu savedMenu = captor.getValue();
        assertThat(savedMenu.getCalories()).isEqualTo(850);
        assertThat(savedMenu.getSodium()).isEqualTo(650);
        assertThat(savedMenu.getSugar()).isEqualTo(18);
    }

    @Test
    void getMenusEnrichesEmptyNutritionAndSavesIt() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        MenuService menuService = new MenuService(menuRepository, placeRepository, menuNutritionClient);
        Menu menu = Menu.builder()
                .placeId(1L)
                .name("Tuna gimbap")
                .menuType(MenuType.MEAL)
                .calories(0)
                .sodium(0)
                .sugar(0)
                .build();

        when(placeRepository.existsById(1L)).thenReturn(true);
        when(menuRepository.findAllByPlaceIdOrderByNameAsc(1L)).thenReturn(List.of(menu));
        when(menuNutritionClient.match("Tuna gimbap")).thenReturn(Optional.of(new MenuNutritionMatchResponse(
                "Tuna gimbap",
                128.4,
                175.2,
                0.57,
                0.64,
                0.905,
                false
        )));
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var responses = menuService.getMenus(1L);

        assertThat(responses).singleElement()
                .satisfies(response -> {
                    assertThat(response.calories()).isEqualTo(128);
                    assertThat(response.sodium()).isEqualTo(175);
                    assertThat(response.sugar()).isEqualTo(1);
                });
        verify(menuRepository).save(menu);
    }

    @Test
    void getMenusDoesNotCallAiWhenNutritionAlreadyExists() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuNutritionClient menuNutritionClient = mock(MenuNutritionClient.class);
        MenuService menuService = new MenuService(menuRepository, placeRepository, menuNutritionClient);
        Menu menu = Menu.builder()
                .placeId(1L)
                .name("Abalone porridge")
                .menuType(MenuType.MEAL)
                .calories(850)
                .sodium(650)
                .sugar(18)
                .build();

        when(placeRepository.existsById(1L)).thenReturn(true);
        when(menuRepository.findAllByPlaceIdOrderByNameAsc(1L)).thenReturn(List.of(menu));

        var responses = menuService.getMenus(1L);

        assertThat(responses).singleElement()
                .satisfies(response -> {
                    assertThat(response.calories()).isEqualTo(850);
                    assertThat(response.sodium()).isEqualTo(650);
                    assertThat(response.sugar()).isEqualTo(18);
                });
        verify(menuNutritionClient, never()).match(any());
        verify(menuRepository, never()).save(any());
    }
}
