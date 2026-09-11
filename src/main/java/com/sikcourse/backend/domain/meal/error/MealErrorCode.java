package com.sikcourse.backend.domain.meal.error;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MealErrorCode implements BaseErrorCode {

    MENU_NOT_FOUND("MEAL_404", "메뉴를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
