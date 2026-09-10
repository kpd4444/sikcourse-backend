package com.sikcourse.backend.global.error.code;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드 enum이 구현해야 하는 인터페이스.
 * 도메인별 ErrorCode는 이 인터페이스를 구현한다.
 */
public interface BaseErrorCode {

    String getCode();

    String getMessage();

    HttpStatus getStatus();
}
