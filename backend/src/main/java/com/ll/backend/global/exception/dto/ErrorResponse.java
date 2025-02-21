package com.ll.backend.global.exception.dto;

import com.ll.backend.global.exception.base.ErrorCode;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String code,
        String message,
        String path,
        int status
) {
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String path) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        path,
                        errorCode.getStatus().value()
                ));
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String message, String path) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        errorCode.getCode(),
                        message,
                        path,
                        errorCode.getStatus().value()
                ));
    }
}