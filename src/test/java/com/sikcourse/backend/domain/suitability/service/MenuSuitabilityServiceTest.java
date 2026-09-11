package com.sikcourse.backend.domain.suitability.service;

import com.sikcourse.backend.domain.health.entity.ActivityLevel;
import com.sikcourse.backend.domain.health.entity.DietaryRestrictionType;
import com.sikcourse.backend.domain.health.entity.DiseaseType;
import com.sikcourse.backend.domain.health.entity.Gender;
import com.sikcourse.backend.domain.health.entity.HealthProfile;
import com.sikcourse.backend.domain.health.error.HealthErrorCode;
import com.sikcourse.backend.domain.health.repository.HealthProfileRepository;
import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.error.MealErrorCode;
import com.sikcourse.backend.domain.meal.repository.MealRecordRepository;
import com.sikcourse.backend.domain.meal.repository.MenuRepository;
import com.sikcourse.backend.domain.place.error.PlaceErrorCode;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityReasonType;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuSuitabilityServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-11T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    void calculateReturnsExcellentWhenMenuFitsRemainingNutrition() {
        TestContext context = testContext();
        Menu menu = menu(1L, "전복죽", 420, 780, 3);

        when(context.healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile(Set.of(), Set.of())));
        when(context.menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(context.mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                eq(1L),
                any(),
                any()
        )).thenReturn(List.of());

        MenuSuitabilityResponse response = context.service.calculate(1L, 1L);

        assertThat(response.score()).isEqualTo(100);
        assertThat(response.level()).isEqualTo(SuitabilityLevel.EXCELLENT);
        assertThat(response.reasons()).isEmpty();
    }

    @Test
    void calculateStrengthensSugarPenaltyForDiabetesAndLowSugarRestriction() {
        TestContext context = testContext();
        Menu menu = menu(1L, "감귤 디저트", 300, 100, 80);

        when(context.healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile(
                Set.of(DiseaseType.DIABETES),
                Set.of(DietaryRestrictionType.LOW_SUGAR)
        )));
        when(context.menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(context.mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                eq(1L),
                any(),
                any()
        )).thenReturn(List.of());

        MenuSuitabilityResponse response = context.service.calculate(1L, 1L);

        assertThat(response.score()).isEqualTo(43);
        assertThat(response.level()).isEqualTo(SuitabilityLevel.DANGER);
        assertThat(response.reasons()).singleElement()
                .satisfies(reason -> {
                    assertThat(reason.type()).isEqualTo(SuitabilityReasonType.SUGAR);
                    assertThat(reason.penalty()).isEqualTo(57);
                    assertThat(reason.exceededAmount()).isEqualTo(30);
                });
    }

    @Test
    void calculatePlaceMenusReturnsMenusSortedByScoreDesc() {
        TestContext context = testContext();
        Menu goodMenu = menu(1L, "전복죽", 420, 780, 3);
        Menu cautionMenu = menu(2L, "해물라면", 900, 2500, 5);

        when(context.placeRepository.existsById(1L)).thenReturn(true);
        when(context.healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile(
                Set.of(DiseaseType.HYPERTENSION),
                Set.of(DietaryRestrictionType.LOW_SODIUM)
        )));
        when(context.mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                eq(1L),
                any(),
                any()
        )).thenReturn(List.of());
        when(context.menuRepository.findAllByPlaceIdOrderByNameAsc(1L)).thenReturn(List.of(cautionMenu, goodMenu));

        List<MenuSuitabilityResponse> responses = context.service.calculatePlaceMenus(1L, 1L);

        assertThat(responses).extracting(MenuSuitabilityResponse::menuName)
                .containsExactly("전복죽", "해물라면");
    }

    @Test
    void calculateRequiresHealthProfile() {
        TestContext context = testContext();

        when(context.healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> context.service.calculate(1L, 1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(HealthErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }

    @Test
    void calculateRequiresMenu() {
        TestContext context = testContext();

        when(context.healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile(Set.of(), Set.of())));
        when(context.mealRecordRepository.findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
                eq(1L),
                any(),
                any()
        )).thenReturn(List.of());
        when(context.menuRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> context.service.calculate(1L, 1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MealErrorCode.MENU_NOT_FOUND));
    }

    @Test
    void calculatePlaceMenusRequiresPlace() {
        TestContext context = testContext();

        when(context.placeRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> context.service.calculatePlaceMenus(1L, 1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    private TestContext testContext() {
        HealthProfileRepository healthProfileRepository = mock(HealthProfileRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        MealRecordRepository mealRecordRepository = mock(MealRecordRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        MenuSuitabilityService service = new MenuSuitabilityService(
                healthProfileRepository,
                menuRepository,
                mealRecordRepository,
                placeRepository,
                FIXED_CLOCK
        );

        return new TestContext(
                healthProfileRepository,
                menuRepository,
                mealRecordRepository,
                placeRepository,
                service
        );
    }

    private HealthProfile healthProfile(
            Set<DiseaseType> diseases,
            Set<DietaryRestrictionType> dietaryRestrictions
    ) {
        return HealthProfile.builder()
                .userId(1L)
                .birthDate(java.time.LocalDate.of(1998, 5, 20))
                .gender(Gender.MALE)
                .height(175)
                .weight(70)
                .activityLevel(ActivityLevel.MODERATE)
                .diseases(diseases)
                .allergies(Set.of())
                .dietaryRestrictions(dietaryRestrictions)
                .dailyCalorieGoal(2000)
                .dailySodiumGoal(2000)
                .dailySugarGoal(50)
                .build();
    }

    private Menu menu(Long id, String name, Integer calories, Integer sodium, Integer sugar) {
        Menu menu = Menu.builder()
                .placeId(1L)
                .name(name)
                .calories(calories)
                .sodium(sodium)
                .sugar(sugar)
                .build();
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private record TestContext(
            HealthProfileRepository healthProfileRepository,
            MenuRepository menuRepository,
            MealRecordRepository mealRecordRepository,
            PlaceRepository placeRepository,
            MenuSuitabilityService service
    ) {
    }
}
