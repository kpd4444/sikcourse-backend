package com.sikcourse.backend.domain.health.error;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HealthErrorCode implements BaseErrorCode {

    HEALTH_PROFILE_NOT_FOUND("HEALTH_404", "건강 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_HEALTH_PROFILE("HEALTH_409", "이미 건강 프로필이 등록되어 있습니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
