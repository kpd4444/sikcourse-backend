package com.sikcourse.backend.domain.message.service;

import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.meal.entity.MealType;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.dto.SuitabilityReasonResponse;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityLevel;
import com.sikcourse.backend.domain.suitability.entity.SuitabilityReasonType;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.infra.gemini.GeminiClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeminiMessageServiceTest {

    @Test
    void menuRecommendationMessageReturnsGeneratedMessage() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        MenuSuitabilityResponse suitability = new MenuSuitabilityResponse(
                1L,
                "Bibimbap",
                91,
                SuitabilityLevel.EXCELLENT,
                List.of()
        );
        when(geminiClient.generate(contains("Bibimbap"))).thenReturn(Optional.of("Generated menu message"));

        String response = service.menuRecommendationMessage(suitability);

        assertThat(response).isEqualTo("Generated menu message");
    }

    @Test
    void menuRecommendationMessageFallsBackWhenGeminiReturnsEmpty() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        MenuSuitabilityResponse suitability = new MenuSuitabilityResponse(
                1L,
                "Ramen",
                42,
                SuitabilityLevel.DANGER,
                List.of(new SuitabilityReasonResponse(
                        SuitabilityReasonType.SODIUM,
                        "Sodium is high",
                        35,
                        900
                ))
        );
        when(geminiClient.generate(contains("Ramen"))).thenReturn(Optional.empty());

        String response = service.menuRecommendationMessage(suitability);

        assertThat(response).isNotBlank();
    }

    @Test
    void mealCompletionPopupMessageReturnsGeneratedMessage() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        MealRecordResponse mealRecord = mealRecord();
        DailyNutritionSummaryResponse summary = summary(800, 500, 25, 1200, 1500, 35);
        when(geminiClient.generate(contains("Bibimbap"))).thenReturn(Optional.of("Generated popup message"));

        String response = service.mealCompletionPopupMessage(mealRecord, summary, true, false);

        assertThat(response).isEqualTo("Generated popup message");
    }

    @Test
    void dailySummaryMessageFallsBackWhenGeminiReturnsEmpty() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        DailyNutritionSummaryResponse summary = summary(100, 800, 10, -200, 2200, 20);
        when(geminiClient.generate(contains("Bibimbap"))).thenReturn(Optional.empty());

        String response = service.dailySummaryMessage(List.of(mealRecord()), summary);

        assertThat(response).isNotBlank();
    }

    @Test
    void dailySummaryMessageFallsBackWhenGeminiReturnsTooShortText() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        DailyNutritionSummaryResponse summary = summary(100, 800, 10, -200, 2200, 20);
        when(geminiClient.generate(contains("Bibimbap"))).thenReturn(Optional.of("오늘 달"));

        String response = service.dailySummaryMessage(List.of(mealRecord()), summary);

        assertThat(response).isNotEqualTo("오늘 달");
        assertThat(response).isNotBlank();
    }

    @Test
    void placeRecommendationPointReturnsGeneratedMessage() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        when(geminiClient.generate(contains("Jeju Restaurant")))
                .thenReturn(Optional.of("Generated place point"));

        String response = service.placeRecommendationPoint(place());

        assertThat(response).isEqualTo("Generated place point");
    }

    @Test
    void tripCourseMessagesReturnGeneratedMessages() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        GeminiMessageService service = new GeminiMessageService(geminiClient);
        Trip trip = trip();
        when(geminiClient.generate(contains("코스 인사이트")))
                .thenReturn(Optional.of("Generated course message"));
        when(geminiClient.generate(contains("코스 피드백")))
                .thenReturn(Optional.of("Generated feedback message"));

        assertThat(service.tripCourseMessage(trip)).isEqualTo("Generated course message");
        assertThat(service.tripCourseFeedbackMessage(trip)).isEqualTo("Generated feedback message");
    }

    private MealRecordResponse mealRecord() {
        return new MealRecordResponse(
                1L,
                1L,
                "Bibimbap",
                MealType.LUNCH,
                LocalDateTime.of(2026, 9, 16, 12, 30),
                BigDecimal.ONE,
                600,
                700,
                15
        );
    }

    private DailyNutritionSummaryResponse summary(
            Integer calorieRemaining,
            Integer calorieConsumed,
            Integer sodiumRemaining,
            Integer sodiumConsumed,
            Integer sugarRemaining,
            Integer sugarConsumed
    ) {
        return new DailyNutritionSummaryResponse(
                2000,
                calorieConsumed,
                calorieRemaining,
                2000,
                sodiumConsumed,
                sodiumRemaining,
                50,
                sugarConsumed,
                sugarRemaining
        );
    }

    private Place place() {
        return Place.builder()
                .contentId("content-1")
                .contentTypeId("39")
                .placeType(PlaceType.RESTAURANT)
                .title("Jeju Restaurant")
                .addr1("Jeju")
                .addr2("")
                .areaCode("39")
                .sigunguCode("4")
                .mapX(new BigDecimal("126.1234567890"))
                .mapY(new BigDecimal("33.1234567890"))
                .tel("064-000-0000")
                .firstImage("https://example.com/image.jpg")
                .firstImage2("https://example.com/thumb.jpg")
                .cat1("A05")
                .cat2("A0502")
                .cat3("A05020100")
                .build();
    }

    private Trip trip() {
        return Trip.builder()
                .userId(1L)
                .title("Jeju Trip")
                .areaCode("39")
                .sigunguCode("4")
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .baseMapX(new BigDecimal("126.5312"))
                .baseMapY(new BigDecimal("33.4996"))
                .build();
    }
}
