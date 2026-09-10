package com.sikcourse.backend.domain.user.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {

    public static LoginResponse bearer(String accessToken) {
        return new LoginResponse(accessToken, "Bearer");
    }
}
