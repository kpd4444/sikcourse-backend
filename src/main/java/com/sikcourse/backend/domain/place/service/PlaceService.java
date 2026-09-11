package com.sikcourse.backend.domain.place.service;

import com.sikcourse.backend.domain.place.dto.PlaceResponse;
import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TourApiClient tourApiClient;

    @Transactional
    public PlaceSyncResponse syncRestaurants(String areaCode, String sigunguCode, Integer pageNo, Integer numOfRows) {
        List<TourRestaurantItem> items = tourApiClient
                .areaBasedList2(areaCode, sigunguCode, pageNo, numOfRows)
                .body()
                .items();
        int createdCount = 0;
        int updatedCount = 0;

        for (TourRestaurantItem item : items) {
            Place place = placeRepository.findByContentId(item.contentid()).orElse(null);
            if (place == null) {
                placeRepository.save(Place.from(item));
                createdCount++;
            } else {
                place.updateFrom(item);
                updatedCount++;
            }
        }

        return new PlaceSyncResponse(items.size(), createdCount, updatedCount);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> getPlaces(String areaCode, String sigunguCode) {
        return findPlaces(areaCode, sigunguCode).stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaceResponse getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND));
        return PlaceResponse.from(place);
    }

    private List<Place> findPlaces(String areaCode, String sigunguCode) {
        if (isBlank(areaCode)) {
            return placeRepository.findAllByOrderByTitleAsc();
        }
        if (isBlank(sigunguCode)) {
            return placeRepository.findAllByAreaCodeOrderByTitleAsc(areaCode);
        }
        return placeRepository.findAllByAreaCodeAndSigunguCodeOrderByTitleAsc(areaCode, sigunguCode);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
