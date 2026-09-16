package com.sikcourse.backend.domain.message.service;

import com.sikcourse.backend.domain.meal.dto.DailyNutritionSummaryResponse;
import com.sikcourse.backend.domain.meal.dto.MealRecordResponse;
import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.suitability.dto.MenuSuitabilityResponse;
import com.sikcourse.backend.domain.suitability.dto.SuitabilityReasonResponse;
import com.sikcourse.backend.domain.trip.entity.Trip;
import com.sikcourse.backend.infra.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiMessageService {

    private final GeminiClient geminiClient;

    public String menuRecommendationMessage(MenuSuitabilityResponse suitability) {
        String prompt = """
                당신은 만성질환자의 여행 중 식단 관리를 돕는 서비스의 추천 문구 작성자입니다.
                아래 메뉴 적합도 데이터를 바탕으로 사용자가 이해하기 쉬운 한국어 문장 1개를 작성하세요.
                과장하지 말고, 의료 진단처럼 말하지 말고, 60자 이내로 작성하세요.

                메뉴명: %s
                점수: %d
                등급: %s
                감점 사유: %s
                """.formatted(
                suitability.menuName(),
                suitability.score(),
                suitability.level(),
                reasonText(suitability.reasons())
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackMenuMessage(suitability));
    }

    public String mealCompletionPopupMessage(
            MealRecordResponse mealRecord,
            DailyNutritionSummaryResponse summary,
            boolean dessertAvailable,
            boolean walkAvailable
    ) {
        String prompt = """
                당신은 만성질환자의 여행 중 식단 관리를 돕는 서비스의 팝업 문구 작성자입니다.
                식사 완료 직후 보여줄 자연스러운 한국어 문장 1개를 작성하세요.
                디저트 또는 산책 추천 가능 여부를 자연스럽게 반영하고, 70자 이내로 작성하세요.

                식사 유형: %s
                메뉴명: %s
                남은 칼로리: %d
                남은 나트륨: %d
                남은 당류: %d
                디저트 추천 가능: %s
                산책 추천 가능: %s
                """.formatted(
                mealRecord.mealType(),
                mealRecord.menuName(),
                summary.calorieRemaining(),
                summary.sodiumRemaining(),
                summary.sugarRemaining(),
                dessertAvailable,
                walkAvailable
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackPopupMessage(dessertAvailable, walkAvailable));
    }

    public String placeRecommendationPoint(Place place) {
        String prompt = """
                당신은 여행 식단 관리 앱의 AI 추천 포인트 작성자입니다.
                아래 장소 정보를 바탕으로 한국어 문단 1개를 120자 이내로 작성하세요.
                식당이면 건강한 메뉴 선택 관점, 산책 장소면 식후 활동 관점을 중심으로 설명하세요.

                장소명: %s
                주소: %s %s
                카테고리: %s/%s/%s
                장소 유형: %s
                """.formatted(
                place.getTitle(),
                place.getAddr1(),
                place.getAddr2(),
                place.getCat1(),
                place.getCat2(),
                place.getCat3(),
                place.getPlaceType()
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackPlaceRecommendationPoint(place));
    }

    public String dailySummaryMessage(
            List<MealRecordResponse> records,
            DailyNutritionSummaryResponse summary
    ) {
        String mealNames = records.stream()
                .map(MealRecordResponse::menuName)
                .collect(Collectors.joining(", "));
        String prompt = """
                당신은 만성질환자의 여행 중 식단 관리를 돕는 서비스의 하루 요약 작성자입니다.
                오늘 식사 기록과 영양 요약을 바탕으로 한국어 문장 1개를 작성하세요.
                사용자를 비난하지 말고, 다음 식사 조절을 안내하며, 90자 이내로 작성하세요.

                오늘 먹은 메뉴: %s
                남은 칼로리: %d
                남은 나트륨: %d
                남은 당류: %d
                섭취 칼로리: %d
                섭취 나트륨: %d
                섭취 당류: %d
                """.formatted(
                mealNames.isBlank() ? "없음" : mealNames,
                summary.calorieRemaining(),
                summary.sodiumRemaining(),
                summary.sugarRemaining(),
                summary.calorieConsumed(),
                summary.sodiumConsumed(),
                summary.sugarConsumed()
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackDailySummaryMessage(summary));
    }

    public String tripCourseMessage(Trip trip) {
        String prompt = """
                당신은 여행 식단 관리 앱의 코스 인사이트 작성자입니다.
                아래 여행 정보를 바탕으로 한국어 문장 1개를 110자 이내로 작성하세요.
                균형 잡힌 식사와 가벼운 이동을 함께 관리할 수 있다는 관점으로 작성하세요.

                여행명: %s
                지역 코드: %s
                시군구 코드: %s
                시작일: %s
                종료일: %s
                """.formatted(
                trip.getTitle(),
                trip.getAreaCode(),
                trip.getSigunguCode(),
                trip.getStartDate(),
                trip.getEndDate()
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackTripCourseMessage(trip));
    }

    public String tripCourseFeedbackMessage(Trip trip) {
        String prompt = """
                당신은 여행 식단 관리 앱의 코스 피드백 작성자입니다.
                한국어 문장 1개를 90자 이내로 작성하세요.
                안정적인 컨디션, 가벼운 걷기, 다음 식사 균형을 중심으로 작성하세요.

                여행명: %s
                시작일: %s
                종료일: %s
                """.formatted(
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate()
        );

        return geminiClient.generate(prompt)
                .orElseGet(() -> fallbackTripCourseFeedbackMessage(trip));
    }

    private String reasonText(List<SuitabilityReasonResponse> reasons) {
        if (reasons.isEmpty()) {
            return "없음";
        }
        return reasons.stream()
                .map(reason -> "%s %d점 감점, %d 초과".formatted(
                        reason.type(),
                        reason.penalty(),
                        reason.exceededAmount()
                ))
                .collect(Collectors.joining("; "));
    }

    private String fallbackMenuMessage(MenuSuitabilityResponse suitability) {
        if (suitability.reasons().isEmpty()) {
            return "현재 건강 목표 기준으로 비교적 잘 맞는 메뉴입니다.";
        }
        return "일부 영양 기준을 초과할 수 있어 남은 식사는 가볍게 조절해보세요.";
    }

    private String fallbackPopupMessage(boolean dessertAvailable, boolean walkAvailable) {
        if (dessertAvailable) {
            return "식사를 기록했어요. 오늘 남은 영양 기준에 맞는 디저트도 확인해보세요.";
        }
        if (walkAvailable) {
            return "식사를 기록했어요. 가볍게 걸을 수 있는 산책 장소도 확인해보세요.";
        }
        return "식사를 기록했어요. 오늘 남은 영양 상태를 확인하고 다음 식사를 조절해보세요.";
    }

    private String fallbackPlaceRecommendationPoint(Place place) {
        if (place.getPlaceType() != null && place.getPlaceType().name().equals("WALK")) {
            return "가볍게 걷기 좋은 장소라 식후 컨디션 관리에 활용하기 좋아요.";
        }
        return "식사 전후 동선에 넣기 좋은 장소로, 메뉴 선택 시 나트륨과 당류를 함께 확인해보세요.";
    }

    private String fallbackDailySummaryMessage(DailyNutritionSummaryResponse summary) {
        if (summary.sodiumRemaining() < 0) {
            return "오늘은 나트륨 섭취가 목표를 넘었어요. 다음 식사는 조금 담백하게 조절해보세요.";
        }
        if (summary.sugarRemaining() < 0) {
            return "오늘은 당류 섭취가 목표를 넘었어요. 다음 간식은 가볍게 선택해보세요.";
        }
        return "오늘은 주요 영양 목표를 비교적 잘 관리했어요. 남은 식사도 균형 있게 이어가보세요.";
    }

    private String fallbackTripCourseMessage(Trip trip) {
        return "%s 코스는 식사와 가벼운 이동을 함께 조절하며 건강 리듬을 유지하기 좋아요.".formatted(trip.getTitle());
    }

    private String fallbackTripCourseFeedbackMessage(Trip trip) {
        return "%s 일정은 식후 가벼운 활동을 곁들이며 다음 식사 균형을 이어가기 좋아요.".formatted(trip.getTitle());
    }
}
