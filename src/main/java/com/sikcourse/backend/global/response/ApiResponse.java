package com.sikcourse.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sikcourse.backend.global.error.code.BaseErrorCode;

/**
 * 모든 API 응답의 공통 포맷.
 * 성공/실패 여부, 코드, 메시지, 실제 데이터를 감싼다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        String message,
        T result
) {

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, "COMMON_200", "요청에 성공했습니다.", result);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> fail(BaseErrorCode errorCode, T result) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), result);
    }

    public static ApiResponse<Void> fail(BaseErrorCode errorCode) {
        return fail(errorCode, null);
    }
}
