package com.sikcourse.backend.domain.user.service;

import com.sikcourse.backend.domain.user.dto.SignUpRequest;
import com.sikcourse.backend.domain.user.error.UserErrorCode;
import com.sikcourse.backend.domain.user.repository.UserRepository;
import com.sikcourse.backend.global.error.exception.GeneralException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthServiceConcurrencyTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private String email;

    @AfterEach
    void tearDown() {
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(userRepository::delete);
        }
    }

    @Test
    void sameEmailConcurrentSignUpReturnsOneSuccessAndOneDuplicateEmail() throws Exception {
        email = "concurrent-" + System.nanoTime() + "@example.com";
        SignUpRequest request = new SignUpRequest(email, "password1234!", "식코스유저");
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<String> task = () -> {
            startLatch.await();
            try {
                authService.signUp(request);
                return "SUCCESS";
            } catch (GeneralException exception) {
                if (exception.getErrorCode() == UserErrorCode.DUPLICATE_EMAIL) {
                    return "DUPLICATE_EMAIL";
                }
                throw exception;
            }
        };

        Future<String> first = executorService.submit(task);
        Future<String> second = executorService.submit(task);
        startLatch.countDown();

        List<String> results = List.of(first.get(), second.get());
        executorService.shutdown();

        assertThat(results).containsExactlyInAnyOrder("SUCCESS", "DUPLICATE_EMAIL");
    }
}
