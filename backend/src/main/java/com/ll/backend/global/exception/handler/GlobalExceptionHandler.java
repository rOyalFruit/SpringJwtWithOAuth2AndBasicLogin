package com.ll.backend.global.exception.handler;

import com.ll.backend.global.exception.auth.AuthException;
import com.ll.backend.global.exception.auth.token.TokenException;
import com.ll.backend.global.exception.base.BaseException;
import com.ll.backend.global.exception.base.ErrorCode;
import com.ll.backend.global.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Custom Base Exception Handler
     */
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse> handleBaseException(BaseException e, HttpServletRequest request) {
        log.error("handleBaseException", e);
        return ErrorResponse.toResponseEntity(e.getErrorCode(), request.getRequestURI());
    }

    /**
     * Authentication Exception Handler
     */
    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<ErrorResponse> handleAuthException(AuthException e, HttpServletRequest request) {
        log.error("handleAuthException", e);
        return ErrorResponse.toResponseEntity(e.getErrorCode(), request.getRequestURI());
    }

    /**
     * Token Exception Handler
     */
    @ExceptionHandler(TokenException.class)
    protected ResponseEntity<ErrorResponse> handleTokenException(TokenException e, HttpServletRequest request) {
        log.error("handleTokenException", e);
        return ErrorResponse.toResponseEntity(e.getErrorCode(), request.getRequestURI());
    }

    /**
     * Spring Security Authentication Exception Handler
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("handleAuthenticationException", e);
        return ErrorResponse.toResponseEntity(ErrorCode.UNAUTHORIZED_ACCESS, e.getMessage(), request.getRequestURI());
    }

    /**
     * Spring Security Access Denied Exception Handler
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("handleAccessDeniedException", e);
        return ErrorResponse.toResponseEntity(ErrorCode.UNAUTHORIZED_ACCESS, e.getMessage(), request.getRequestURI());
    }

    /**
     * Validation Exception Handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("handleMethodArgumentNotValidException", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult().getAllErrors().getFirst().getDefaultMessage(), request.getRequestURI());
    }

    /**
     * Bind Exception Handler
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        log.error("handleBindException", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult().getAllErrors().getFirst().getDefaultMessage(), request.getRequestURI());
    }

    /**
     * General Exception Handler
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("handleException", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }
}