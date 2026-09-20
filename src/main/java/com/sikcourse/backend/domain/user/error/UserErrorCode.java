package com.sikcourse.backend.domain.user.error;

import com.sikcourse.backend.global.error.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    DUPLICATE_EMAIL("USER_409", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_LOGIN("USER_401", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD("USER_401", "현재 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("USER_404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
