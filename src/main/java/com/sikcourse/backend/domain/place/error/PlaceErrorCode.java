package com.sikcourse.backend.domain.place.error;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements BaseErrorCode {

    PLACE_NOT_FOUND("PLACE_404", "Place not found.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
