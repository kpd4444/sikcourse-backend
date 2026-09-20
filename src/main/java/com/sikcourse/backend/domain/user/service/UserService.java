package com.sikcourse.backend.domain.user.service;

import com.sikcourse.backend.domain.user.dto.ChangePasswordRequest;
import com.sikcourse.backend.domain.user.dto.UserResponse;
import com.sikcourse.backend.domain.user.entity.User;
import com.sikcourse.backend.domain.user.error.UserErrorCode;
import com.sikcourse.backend.domain.user.repository.UserRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getActiveUser(userId);
        if (!passwordHasher.matches(request.currentPassword(), user.getPassword())) {
            throw new GeneralException(UserErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(passwordHasher.hash(request.newPassword()));
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = getActiveUser(userId);
        user.withdraw(LocalDateTime.now());
    }

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
    }
}
