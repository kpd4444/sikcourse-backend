package com.sikcourse.backend.domain.recommendation.service;

import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.entity.MenuType;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedMenuResponse;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedPlaceResponse;
import com.sikcourse.backend.domain.recommendation.dto.RecommendedWalkResponse;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.service.MenuSuitabilityContext;
import com.sikcourse.backend.domain.suitability.service.MenuSuitabilityService;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.domain.trip.error.TripErrorCode;
import com.sikcourse.backend.domain.trip.repository.TripRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final MenuRepository menuRepository;
    private final MenuSuitabilityService menuSuitabilityService;

    @Transactional(readOnly = true)
    public List<RecommendedMenuResponse> recommendMenus(Long userId, Long tripId) {
        return recommendMenus(userId, tripId, null);
    }

    @Transactional(readOnly = true)
    public List<RecommendedMenuResponse> recommendDesserts(Long userId, Long tripId) {
        return recommendMenus(userId, tripId, MenuType.DESSERT);
    }

    private List<RecommendedMenuResponse> recommendMenus(Long userId, Long tripId, MenuType menuType) {
        Trip trip = getTrip(userId, tripId);
        List<Place> places = findTripPlaces(trip);
        Map<Long, Place> placesById = places.stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));
        if (places.isEmpty()) {
            return List.of();
        }

        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();
        List<Menu> menus = findMenus(placeIds, menuType);
        MenuSuitabilityContext context = menuSuitabilityService.createContext(userId);

        return menus.stream()
                .map(menu -> toRecommendedMenu(context, placesById.get(menu.getPlaceId()), menu))
                .sorted(menuRecommendationComparator())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendedPlaceResponse> recommendPlaces(Long userId, Long tripId) {
        return recommendMenus(userId, tripId).stream()
                .collect(Collectors.groupingBy(
                        RecommendedMenuResponse::placeId,
                        Collectors.minBy(menuRecommendationComparator())
                ))
                .values()
                .stream()
                .flatMap(java.util.Optional::stream)
                .map(menu -> toRecommendedPlace(menu, findPlace(menu.placeId())))
                .sorted(Comparator
                        .comparing((RecommendedPlaceResponse place) -> place.bestMenu().score()).reversed()
                        .thenComparing(RecommendedPlaceResponse::placeName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendedWalkResponse> recommendWalks(Long userId, Long tripId) {
        Trip trip = getTrip(userId, tripId);
        return findTripPlaces(trip, PlaceType.WALK).stream()
                .map(this::toRecommendedWalk)
                .toList();
    }

    private RecommendedMenuResponse toRecommendedMenu(MenuSuitabilityContext context, Place place, Menu menu) {
        MenuSuitabilityResponse suitability = menuSuitabilityService.calculate(context, menu);
        return new RecommendedMenuResponse(
                place.getId(),
                place.getTitle(),
                suitability.menuId(),
                suitability.menuName(),
                menu.getMenuType(),
                suitability.score(),
                suitability.level(),
                suitability.reasons()
        );
    }

    private RecommendedPlaceResponse toRecommendedPlace(RecommendedMenuResponse bestMenu, Place place) {
        return new RecommendedPlaceResponse(
                place.getId(),
                place.getTitle(),
                place.getAddr1(),
                place.getMapX(),
                place.getMapY(),
                place.getFirstImage(),
                bestMenu
        );
    }

    private Trip getTrip(Long userId, Long tripId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new GeneralException(TripErrorCode.TRIP_NOT_FOUND));
    }

    private List<Place> findTripPlaces(Trip trip) {
        return findTripPlaces(trip, null);
    }

    private List<Place> findTripPlaces(Trip trip, PlaceType placeType) {
        if (placeType != null) {
            if (trip.getSigunguCode() == null || trip.getSigunguCode().isBlank()) {
                return placeRepository.findAllByAreaCodeAndPlaceTypeOrderByTitleAsc(trip.getAreaCode(), placeType);
            }
            return placeRepository.findAllByAreaCodeAndSigunguCodeAndPlaceTypeOrderByTitleAsc(
                    trip.getAreaCode(),
                    trip.getSigunguCode(),
                    placeType
            );
        }

        if (trip.getSigunguCode() == null || trip.getSigunguCode().isBlank()) {
            return placeRepository.findAllByAreaCodeOrderByTitleAsc(trip.getAreaCode());
        }
        return placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc(
                trip.getAreaCode(),
                trip.getSigunguCode()
        );
    }

    private Place findPlace(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalStateException("추천 장소를 찾을 수 없습니다."));
    }

    private RecommendedWalkResponse toRecommendedWalk(Place place) {
        return new RecommendedWalkResponse(
                place.getId(),
                place.getTitle(),
                place.getAddr1(),
                place.getMapX(),
                place.getMapY(),
                place.getFirstImage()
        );
    }

    private List<Menu> findMenus(List<Long> placeIds, MenuType menuType) {
        if (menuType == null) {
            return menuRepository.findAllByPlaceIdInOrderByNameAsc(placeIds);
        }
        return menuRepository.findAllByPlaceIdInAndMenuTypeOrderByNameAsc(placeIds, menuType);
    }

    private Comparator<RecommendedMenuResponse> menuRecommendationComparator() {
        return Comparator
                .comparing(RecommendedMenuResponse::score).reversed()
                .thenComparing(RecommendedMenuResponse::menuName);
    }
}
