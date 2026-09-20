package com.sikcourse.backend.domain.place.controller;

import com.sikcourse.backend.domain.place.dto.PlaceSyncResponse;
import com.sikcourse.backend.domain.place.service.PlaceService;
import com.sikcourse.backend.global.config.AdminProperties;
import com.sikcourse.backend.global.error.code.GlobalErrorCode;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlaceAdminControllerTest {

    @Test
    void syncRestaurantsRequiresAdminSecret() {
        PlaceService placeService = mock(PlaceService.class);
        PlaceAdminController controller = new PlaceAdminController(
                placeService,
                new AdminProperties("secret")
        );

        assertThatThrownBy(() -> controller.syncRestaurants(null, "39", "4", 1, 10))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.FORBIDDEN));
        verifyNoInteractions(placeService);
    }

    @Test
    void syncRestaurantsCallsServiceWhenAdminSecretMatches() {
        PlaceService placeService = mock(PlaceService.class);
        PlaceAdminController controller = new PlaceAdminController(
                placeService,
                new AdminProperties("secret")
        );
        PlaceSyncResponse syncResponse = new PlaceSyncResponse(10, 8, 2);

        when(placeService.syncRestaurants("39", "4", 1, 10)).thenReturn(syncResponse);

        ApiResponse<PlaceSyncResponse> response = controller.syncRestaurants("secret", "39", "4", 1, 10);

        assertThat(response.result()).isEqualTo(syncResponse);
        verify(placeService).syncRestaurants("39", "4", 1, 10);
    }

    @Test
    void syncWalksRejectsRequestWhenConfiguredSecretIsBlank() {
        PlaceService placeService = mock(PlaceService.class);
        PlaceAdminController controller = new PlaceAdminController(
                placeService,
                new AdminProperties("")
        );

        assertThatThrownBy(() -> controller.syncWalks("secret", "39", "4", 1, 10))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.FORBIDDEN));
        verifyNoInteractions(placeService);
    }
}
