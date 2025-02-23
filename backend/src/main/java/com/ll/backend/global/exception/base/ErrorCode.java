package com.ll.backend.global.exception.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),

    // Authentication Errors
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Token has expired"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A003", "Token not found"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "A004", "Invalid login credentials"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "A005", "Unauthorized access"),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A006", "Failed to generate token"),
    LOGOUT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A007", "An error occurred during logout processing."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A008", "Authentication failed"),

    // Business Errors
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "Resource not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "B002", "Resource already exists");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
