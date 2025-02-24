package com.ll.backend.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.backend.global.exception.base.ErrorCode;
import com.ll.backend.global.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private record AuthenticationErrorInfo(ErrorCode errorCode, String message) {}

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        AuthenticationErrorInfo errorInfo = getAuthenticationErrorInfo(request);
        sendErrorResponse(response, errorInfo, request.getRequestURI());
        logError(errorInfo);
    }

    private AuthenticationErrorInfo getAuthenticationErrorInfo(HttpServletRequest request) {
        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");
        if (errorCode == null) {
            errorCode = ErrorCode.AUTHENTICATION_FAILED;
        }

        return new AuthenticationErrorInfo(errorCode, errorCode.getMessage());
    }

    private void sendErrorResponse(HttpServletResponse response, AuthenticationErrorInfo errorInfo,
                                   String path) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.toResponseEntity(
                errorInfo.errorCode(),
                errorInfo.message(),
                path
        ).getBody();

        response.setStatus(errorInfo.errorCode().getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private void logError(AuthenticationErrorInfo errorInfo) {
        log.error("[Authentication Error] {} : {}", errorInfo.errorCode(), errorInfo.message());
    }
}
