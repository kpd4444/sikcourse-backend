package com.sikcourse.backend.domain.user.service;

import com.sikcourse.backend.domain.user.dto.LoginRequest;
import com.sikcourse.backend.domain.user.dto.LoginResponse;
import com.sikcourse.backend.domain.user.dto.SignUpRequest;
import com.sikcourse.backend.domain.user.dto.UserResponse;
import com.sikcourse.backend.domain.user.entity.User;
import com.sikcourse.backend.domain.user.entity.UserRole;
import com.sikcourse.backend.domain.user.error.UserErrorCode;
import com.sikcourse.backend.domain.user.repository.UserRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.security.JwtTokenProvider;
import com.sikcourse.backend.global.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "pbkdf2$600000$MDEyMzQ1Njc4OWFiY2RlZg==$ot3q54C62oq/HF90Ue1ri93+mASv/CH2OLm4sRcmX1Q=";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new GeneralException(UserErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordHasher.hash(request.password()))
                .nickname(request.nickname())
                .role(UserRole.USER)
                .build();

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueConstraintViolation(exception)) {
                throw new GeneralException(UserErrorCode.DUPLICATE_EMAIL);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        String encodedPassword = (user == null) ? DUMMY_PASSWORD_HASH : user.getPassword();
        boolean passwordMatches = passwordHasher.matches(request.password(), encodedPassword);

        if (user == null || !passwordMatches) {
            throw new GeneralException(UserErrorCode.INVALID_LOGIN);
        }

        return LoginResponse.bearer(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()));
    }

    private boolean isEmailUniqueConstraintViolation(DataIntegrityViolationException exception) {
        String normalizedMessage = collectExceptionMessages(exception).toLowerCase();
        return normalizedMessage.contains("uk_users_email")
                || (normalizedMessage.contains("duplicate")
                && normalizedMessage.contains("users")
                && normalizedMessage.contains("email"));
    }

    private String collectExceptionMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                builder.append(current.getMessage()).append(' ');
            }
            current = current.getCause();
        }
        return builder.toString();
    }
}
