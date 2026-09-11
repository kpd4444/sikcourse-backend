package com.sikcourse.backend.domain.place.service;

import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourApiResponse;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    @Test
    void syncRestaurantsUpdatesExistingPlaceWhenConcurrentInsertCreatesDuplicateContentId() {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        TourApiClient tourApiClient = mock(TourApiClient.class);
        PlaceUpsertService placeUpsertService = mock(PlaceUpsertService.class);
        PlaceService placeService = new PlaceService(placeRepository, tourApiClient, placeUpsertService);
        TourRestaurantItem item = restaurantItem();

        when(tourApiClient.areaBasedList2("39", "4", 1, 10)).thenReturn(tourApiResponse(item));
        when(placeUpsertService.upsert(item, PlaceType.RESTAURANT))
                .thenThrow(new DuplicatePlaceContentIdException("123456"));
        when(placeUpsertService.updateExisting(item, PlaceType.RESTAURANT)).thenReturn(PlaceUpsertResult.UPDATED);

        PlaceSyncResponse response = placeService.syncRestaurants("39", "4", 1, 10);

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isZero();
        assertThat(response.updatedCount()).isEqualTo(1);
        verify(placeUpsertService).updateExisting(item, PlaceType.RESTAURANT);
    }

    @Test
    void syncWalksStoresPlacesAsWalkType() {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        TourApiClient tourApiClient = mock(TourApiClient.class);
        PlaceUpsertService placeUpsertService = mock(PlaceUpsertService.class);
        PlaceService placeService = new PlaceService(placeRepository, tourApiClient, placeUpsertService);
        TourRestaurantItem item = restaurantItem();

        when(tourApiClient.walkAreaBasedList2("39", "4", 1, 10)).thenReturn(tourApiResponse(item));
        when(placeUpsertService.upsert(item, PlaceType.WALK)).thenReturn(PlaceUpsertResult.CREATED);

        PlaceSyncResponse response = placeService.syncWalks("39", "4", 1, 10);

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isZero();
        verify(placeUpsertService).upsert(item, PlaceType.WALK);
    }

    private TourApiResponse<TourRestaurantItem> tourApiResponse(TourRestaurantItem item) {
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
}
