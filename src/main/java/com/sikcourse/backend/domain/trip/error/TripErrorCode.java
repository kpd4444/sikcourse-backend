package com.sikcourse.backend.domain.trip.error;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripErrorCode implements BaseErrorCode {

    TRIP_NOT_FOUND("TRIP_404", "Trip not found.", HttpStatus.NOT_FOUND),
    INVALID_TRIP_DATE_RANGE("TRIP_400", "Trip start date must be before or equal to end date.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
