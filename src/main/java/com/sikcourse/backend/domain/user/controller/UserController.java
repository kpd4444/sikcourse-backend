package com.sikcourse.backend.domain.user.controller;

import com.sikcourse.backend.domain.user.dto.ChangePasswordRequest;
import com.sikcourse.backend.domain.user.dto.UserResponse;
import com.sikcourse.backend.domain.user.service.UserService;
import com.sikcourse.backend.global.response.ApiResponse;
import com.sikcourse.backend.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "Change password")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authUser.userId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "Withdraw account")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal AuthUser authUser) {
        userService.withdraw(authUser.userId());
        return ApiResponse.success();
    }
}
