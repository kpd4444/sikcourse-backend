package com.sikcourse.backend.domain.user.service;

import com.sikcourse.backend.domain.user.dto.ChangePasswordRequest;
import com.sikcourse.backend.domain.user.entity.User;
import com.sikcourse.backend.domain.user.entity.UserRole;
import com.sikcourse.backend.domain.user.error.UserErrorCode;
import com.sikcourse.backend.domain.user.repository.UserRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void changePasswordUpdatesHashedPasswordWhenCurrentPasswordMatches() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);
        UserService userService = new UserService(userRepository, passwordHasher);
        User user = user();

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordHasher.hash("new-password")).thenReturn("new-hash");

        userService.changePassword(1L, new ChangePasswordRequest("old-password", "new-password"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void changePasswordRejectsInvalidCurrentPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);
        UserService userService = new UserService(userRepository, passwordHasher);
        User user = user();

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(
                1L,
                new ChangePasswordRequest("wrong-password", "new-password")
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PASSWORD));
    }

    @Test
    void withdrawMarksUserDeletedAndAnonymizesProfile() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository, mock(PasswordHasher.class));
        User user = user();

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getEmail()).startsWith("withdrawn-");
        assertThat(user.getNickname()).isEqualTo("withdrawn");
        assertThat(user.getDeletedAt()).isNotNull();
        verify(userRepository).findByIdAndDeletedAtIsNull(1L);
    }

    private User user() {
        return User.builder()
                .email("test@example.com")
                .password("old-hash")
                .nickname("tester")
                .role(UserRole.USER)
                .build();
    }
}
