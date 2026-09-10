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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

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

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new GeneralException(UserErrorCode.INVALID_LOGIN));

        if (!passwordHasher.matches(request.password(), user.getPassword())) {
            throw new GeneralException(UserErrorCode.INVALID_LOGIN);
        }

        return LoginResponse.bearer(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()));
    }
}
