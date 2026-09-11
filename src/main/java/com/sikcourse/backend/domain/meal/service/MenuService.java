package com.sikcourse.backend.domain.meal.service;

import com.sikcourse.backend.domain.meal.dto.CreateMenuRequest;
import com.sikcourse.backend.domain.meal.dto.MenuResponse;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.error.MealErrorCode;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public MenuResponse create(Long placeId, CreateMenuRequest request) {
        if (!placeRepository.existsById(placeId)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        Menu menu = Menu.builder()
                .placeId(placeId)
                .name(request.name())
                .menuType(request.menuType())
                .calories(request.calories())
                .sodium(request.sodium())
                .sugar(request.sugar())
                .build();

        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        return menuRepository.findAllByPlaceIdOrderByNameAsc(placeId).stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long menuId) {
        return MenuResponse.from(getById(menuId));
    }

    public Menu getById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new GeneralException(MealErrorCode.MENU_NOT_FOUND));
    }
}
