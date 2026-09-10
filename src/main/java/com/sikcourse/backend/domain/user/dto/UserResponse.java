package com.sikcourse.backend.domain.user.dto;

import com.sikcourse.backend.domain.user.entity.User;

public record UserResponse(
        Long userId,
        String email,
        String nickname
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
