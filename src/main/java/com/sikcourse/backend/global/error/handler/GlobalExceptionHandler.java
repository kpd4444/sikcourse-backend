package com.sikcourse.backend.global.error.handler;

import com.sikcourse.backend.global.error.code.GlobalErrorCode;
import com.sikcourse.backend.global.error.exception.GeneralException;
import com.sikcourse.backend.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException exception) {
        log.warn("GeneralException: {}", exception.getMessage());
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(ApiResponse.fail(exception.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = (fieldError == null)
                ? GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()
                : fieldError.getDefaultMessage();

        return invalidInputValueResponse(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        return invalidInputValueResponse(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        return invalidInputValueResponse(exception.getParameterName() + " is required.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<ApiResponse<Void>> invalidInputValueResponse(String message) {
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(new ApiResponse<>(
                        false,
                        GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                        message,
                        null));
    }
}
