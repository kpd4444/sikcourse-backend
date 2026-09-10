package com.sikcourse.backend.domain.user.controller;

import com.sikcourse.backend.domain.user.dto.UserResponse;
import com.sikcourse.backend.domain.user.service.UserService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(userService.getMyInfo(authUser.userId()));
    }
}
