package com.sikcourse.backend.domain.place.service;

import com.sikcourse.backend.domain.meal.dto.MenuResponse;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.entity.MenuType;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.dto.PlaceResponse;
import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.message.service.GeminiMessageService;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.infra.aimenu.MenuNutritionClient;
import com.sikcourse.backend.infra.aimenu.MenuNutritionMatchResponse;
import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourDetailIntroItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final int AUTO_SYNC_PAGE_NO = 1;
    private static final int AUTO_SYNC_NUM_OF_ROWS = 100;
    private static final List<String> DESSERT_MENU_KEYWORDS = List.of(
            "coffee",
            "latte",
            "ade",
            "juice",
            "smoothie",
            "tea",
            "cake",
            "cookie",
            "waffle",
            "croffle",
            "macaron",
            "gelato",
            "ice cream",
            "dessert",
            "bakery",
            "커피",
            "라떼",
            "에이드",
            "주스",
            "스무디",
            "아이스크림",
            "케이크",
            "쿠키",
            "와플",
            "크로플",
            "마카롱",
            "젤라또",
            "디저트",
            "베이커리",
            "빵"
    );
    private static final Pattern HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern MENU_SEPARATOR_PATTERN = Pattern.compile("[,，/·ㆍ\\n\\r]+");

    private final PlaceRepository placeRepository;
    private final MenuRepository menuRepository;
    private final TourApiClient tourApiClient;
    private final PlaceUpsertService placeUpsertService;
    private final GeminiMessageService geminiMessageService;
    private final MenuNutritionClient menuNutritionClient;

    public PlaceSyncResponse syncRestaurants(String areaCode, String sigunguCode, Integer pageNo, Integer numOfRows) {
        List<TourRestaurantItem> items = tourApiClient
                .areaBasedList2(areaCode, sigunguCode, pageNo, numOfRows)
                .body()
                .items();
        return sync(items, PlaceType.RESTAURANT);
    }

    public PlaceSyncResponse syncWalks(String areaCode, String sigunguCode, Integer pageNo, Integer numOfRows) {
        List<TourRestaurantItem> items = tourApiClient
                .walkAreaBasedList2(areaCode, sigunguCode, pageNo, numOfRows)
                .body()
                .items();
        return sync(items, PlaceType.WALK);
    }

    private PlaceSyncResponse sync(List<TourRestaurantItem> items, PlaceType placeType) {
        int createdCount = 0;
        int updatedCount = 0;

        for (TourRestaurantItem item : items) {
            PlaceUpsertResult result = upsert(item, placeType);
            syncMenusIfRestaurant(item, placeType);
            if (result == PlaceUpsertResult.CREATED) {
                createdCount++;
            } else {
                updatedCount++;
            }
        }

        return new PlaceSyncResponse(items.size(), createdCount, updatedCount);
    }

    @Transactional
    public List<PlaceResponse> getPlaces(String areaCode, String sigunguCode) {
        return getPlaces(areaCode, sigunguCode, null);
    }

    @Transactional
    public List<PlaceResponse> getPlaces(String areaCode, String sigunguCode, PlaceType placeType) {
        List<Place> places = findPlaces(areaCode, sigunguCode, placeType);
        if (places.isEmpty() && !isBlank(areaCode)) {
            syncPlacesSafely(areaCode, sigunguCode, placeType);
            places = findPlaces(areaCode, sigunguCode, placeType);
        }

        return places.stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional
    public PlaceResponse getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND));
        syncMenus(place);
        return PlaceResponse.from(
                place,
                geminiMessageService.placeRecommendationPoint(place),
                menuRepository.findAllByPlaceIdOrderByNameAsc(placeId).stream()
                        .map(MenuResponse::from)
                        .toList()
        );
    }

    private List<Place> findPlaces(String areaCode, String sigunguCode) {
        return findPlaces(areaCode, sigunguCode, null);
    }

    private List<Place> findPlaces(String areaCode, String sigunguCode, PlaceType placeType) {
        if (placeType != null) {
            if (isBlank(areaCode)) {
                return placeRepository.findAllByOrderByTitleAsc().stream()
                        .filter(place -> place.getPlaceType() == placeType)
                        .toList();
            }
            if (isBlank(sigunguCode)) {
                return placeRepository.findAllByAreaCodeAndPlaceTypeOrderByTitleAsc(areaCode, placeType);
            }
            return placeRepository.findAllByAreaCodeAndSigunguCodeAndPlaceTypeOrderByTitleAsc(
                    areaCode,
                    sigunguCode,
                    placeType
            );
        }

        if (isBlank(areaCode)) {
            return placeRepository.findAllByOrderByTitleAsc();
        }
        if (isBlank(sigunguCode)) {
            return placeRepository.findAllByAreaCodeOrderByTitleAsc(areaCode);
        }
        return placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc(areaCode, sigunguCode);
    }

    public void syncRestaurantsSafely(String areaCode, String sigunguCode, Integer pageNo, Integer numOfRows) {
        try {
            syncRestaurants(areaCode, sigunguCode, pageNo, numOfRows);
        } catch (Exception exception) {
            log.warn("Auto restaurant sync failed: areaCode={}, sigunguCode={}, {}: {}",
                    areaCode,
                    sigunguCode,
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
        }
    }

    public void syncWalksSafely(String areaCode, String sigunguCode, Integer pageNo, Integer numOfRows) {
        try {
            syncWalks(areaCode, sigunguCode, pageNo, numOfRows);
        } catch (Exception exception) {
            log.warn("Auto walk sync failed: areaCode={}, sigunguCode={}, {}: {}",
                    areaCode,
                    sigunguCode,
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
        }
    }

    private void syncPlacesSafely(String areaCode, String sigunguCode, PlaceType placeType) {
        if (placeType == null || placeType == PlaceType.RESTAURANT) {
            syncRestaurantsSafely(areaCode, sigunguCode, AUTO_SYNC_PAGE_NO, AUTO_SYNC_NUM_OF_ROWS);
        }
        if (placeType == null || placeType == PlaceType.WALK) {
            syncWalksSafely(areaCode, sigunguCode, AUTO_SYNC_PAGE_NO, AUTO_SYNC_NUM_OF_ROWS);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private PlaceUpsertResult upsert(TourRestaurantItem item, PlaceType placeType) {
        try {
            return placeUpsertService.upsert(item, placeType);
        } catch (DuplicatePlaceContentIdException exception) {
            return placeUpsertService.updateExisting(item, placeType);
        }
    }

    private void syncMenusIfRestaurant(TourRestaurantItem item, PlaceType placeType) {
        if (placeType != PlaceType.RESTAURANT) {
            return;
        }

        placeRepository.findByContentId(item.contentid()).ifPresent(this::syncMenus);
    }

    private void syncMenus(Place place) {
        if (place.getPlaceType() != PlaceType.RESTAURANT) {
            return;
        }

        TourDetailIntroItem intro = findDetailIntro(place.getContentId()).orElse(null);
        if (intro == null) {
            return;
        }

        extractMenuNames(intro).forEach(menuName -> createMenuIfAbsent(place.getId(), menuName));
    }

    private Optional<TourDetailIntroItem> findDetailIntro(String contentId) {
        try {
            return tourApiClient.detailIntro2(contentId).body().items().stream()
                    .findFirst();
        } catch (Exception exception) {
            log.warn("TourAPI detail intro failed for contentId={}: {}: {}",
                    contentId,
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
            return Optional.empty();
        }
    }

    private List<String> extractMenuNames(TourDetailIntroItem intro) {
        return List.of(intro.firstmenu(), intro.treatmenu()).stream()
                .flatMap(value -> splitMenuNames(value).stream())
                .distinct()
                .toList();
    }

    private List<String> splitMenuNames(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String normalized = HTML_BREAK_PATTERN.matcher(value).replaceAll("\n");
        normalized = HTML_TAG_PATTERN.matcher(normalized).replaceAll("");
        return MENU_SEPARATOR_PATTERN.splitAsStream(normalized)
                .map(String::trim)
                .filter(menuName -> !menuName.isBlank())
                .map(this::truncateMenuName)
                .distinct()
                .toList();
    }

    private String truncateMenuName(String menuName) {
        if (menuName.length() <= 100) {
            return menuName;
        }
        return menuName.substring(0, 100);
    }

    private void createMenuIfAbsent(Long placeId, String menuName) {
        Optional<Menu> existingMenu = menuRepository.findByPlaceIdAndNameIgnoreCase(placeId, menuName);
        if (existingMenu.isPresent()) {
            enrichExistingMenu(existingMenu.get(), menuName);
            return;
        }

        MenuNutrition nutrition = matchNutrition(menuName);
        menuRepository.save(Menu.builder()
                .placeId(placeId)
                .name(menuName)
                .menuType(classifyMenuType(menuName))
                .calories(nutrition.calories())
                .sodium(nutrition.sodium())
                .sugar(nutrition.sugar())
                .build());
    }

    private void enrichExistingMenu(Menu menu, String menuName) {
        MenuType menuType = classifyMenuType(menuName);
        boolean changed = false;
        if (menu.getMenuType() != menuType) {
            menu.updateMenuType(menuType);
            changed = true;
        }

        if (menu.getCalories() != 0 && menu.getSodium() != 0 && menu.getSugar() != 0) {
            if (changed) {
                menuRepository.save(menu);
            }
            return;
        }

        MenuNutrition nutrition = matchNutrition(menu.getName());
        if (menu.getCalories().equals(nutrition.calories())
                && menu.getSodium().equals(nutrition.sodium())
                && menu.getSugar().equals(nutrition.sugar())) {
            if (changed) {
                menuRepository.save(menu);
            }
            return;
        }

        menu.updateNutrition(nutrition.calories(), nutrition.sodium(), nutrition.sugar());
        menuRepository.save(menu);
    }

    private MenuType classifyMenuType(String menuName) {
        String normalized = menuName.toLowerCase(Locale.ROOT);
        boolean dessert = DESSERT_MENU_KEYWORDS.stream()
                .anyMatch(normalized::contains);
        return dessert ? MenuType.DESSERT : MenuType.MEAL;
    }

    private MenuNutrition matchNutrition(String menuName) {
        Optional<MenuNutritionMatchResponse> nutrition = menuNutritionClient.match(menuName);
        return new MenuNutrition(
                nutrition.map(MenuNutritionMatchResponse::kcal).map(this::roundToInteger).orElse(0),
                nutrition.map(MenuNutritionMatchResponse::sodium_mg).map(this::roundToInteger).orElse(0),
                nutrition.map(MenuNutritionMatchResponse::sugar_g).map(this::roundToInteger).orElse(0)
        );
    }

    private Integer roundToInteger(Double value) {
        return (int) Math.round(value);
    }

    private record MenuNutrition(Integer calories, Integer sodium, Integer sugar) {
    }
}
