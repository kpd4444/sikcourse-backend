package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.meal.dto.CreateMenuRequest;
import com.sikcourse.backend.domain.meal.dto.MenuResponse;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.error.MealErrorCode;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.infra.aimenu.MenuNutritionClient;
import com.sikcourse.backend.infra.aimenu.MenuNutritionMatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final PlaceRepository placeRepository;
    private final MenuNutritionClient menuNutritionClient;

    @Transactional
    public MenuResponse create(Long placeId, CreateMenuRequest request) {
        if (!placeRepository.existsById(placeId)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        MenuNutrition nutrition = matchNutrition(
                request.name(),
                request.calories(),
                request.sodium(),
                request.sugar()
        );

        Menu menu = Menu.builder()
                .placeId(placeId)
                .name(request.name())
                .menuType(request.menuType())
                .calories(nutrition.calories())
                .sodium(nutrition.sodium())
                .sugar(nutrition.sugar())
                .build();

        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public List<MenuResponse> getMenus(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        return menuRepository.findAllByPlaceIdOrderByNameAsc(placeId).stream()
                .map(this::enrichNutritionIfEmpty)
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public MenuResponse getMenu(Long menuId) {
        return MenuResponse.from(enrichNutritionIfEmpty(getById(menuId)));
    }

    public Menu getById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new GeneralException(MealErrorCode.MENU_NOT_FOUND));
    }

    private Integer roundToInteger(Double value) {
        return (int) Math.round(value);
    }

    private Menu enrichNutritionIfEmpty(Menu menu) {
        if (!isEmptyNutrition(menu)) {
            return menu;
        }

        MenuNutrition nutrition = matchNutrition(
                menu.getName(),
                menu.getCalories(),
                menu.getSodium(),
                menu.getSugar()
        );
        if (nutrition.sameAs(menu)) {
            return menu;
        }

        menu.updateNutrition(nutrition.calories(), nutrition.sodium(), nutrition.sugar());
        log.info(
                "Menu nutrition enriched: menuId={}, menuName={}, calories={}, sodium={}, sugar={}",
                menu.getId(),
                menu.getName(),
                nutrition.calories(),
                nutrition.sodium(),
                nutrition.sugar()
        );
        return menuRepository.save(menu);
    }

    private boolean isEmptyNutrition(Menu menu) {
        return menu.getCalories() == 0
                || menu.getSodium() == 0
                || menu.getSugar() == 0;
    }

    private MenuNutrition matchNutrition(String menuName, Integer calories, Integer sodium, Integer sugar) {
        Optional<MenuNutritionMatchResponse> nutrition = menuNutritionClient.match(menuName);
        if (nutrition.isEmpty()) {
            log.info("AI menu nutrition not available: menuName={}", menuName);
        }
        return new MenuNutrition(
                nutrition.map(MenuNutritionMatchResponse::kcal)
                        .map(this::roundToInteger)
                        .orElse(calories),
                nutrition.map(MenuNutritionMatchResponse::sodium_mg)
                        .map(this::roundToInteger)
                        .orElse(sodium),
                nutrition.map(MenuNutritionMatchResponse::sugar_g)
                        .map(this::roundToInteger)
                        .orElse(sugar)
        );
    }

    private record MenuNutrition(Integer calories, Integer sodium, Integer sugar) {

        private boolean sameAs(Menu menu) {
            return calories.equals(menu.getCalories())
                    && sodium.equals(menu.getSodium())
                    && sugar.equals(menu.getSugar());
        }
    }
}
