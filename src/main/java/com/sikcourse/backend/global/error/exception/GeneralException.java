package com.sikcourse.backend.global.error.exception;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직에서 의도적으로 던지는 예외.
 * 서비스 계층에서 throw new GeneralException(UserErrorCode.XXX) 형태로 사용한다.
 */
@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public GeneralException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
