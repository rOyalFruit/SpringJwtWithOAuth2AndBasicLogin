package com.ll.backend.global.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.backend.global.exception.base.ErrorCode;
import com.ll.backend.global.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    private record AuthorizationErrorInfo(ErrorCode errorCode, String message) {}

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        AuthorizationErrorInfo errorInfo = getAuthorizationErrorInfo(request);
        sendErrorResponse(response, errorInfo, request.getRequestURI());
        logError(errorInfo);
    }

    private AuthorizationErrorInfo getAuthorizationErrorInfo(HttpServletRequest request) {
        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");
        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED_ACCESS;
        }

        return new AuthorizationErrorInfo(errorCode, errorCode.getMessage());
    }

    private void sendErrorResponse(HttpServletResponse response, AuthorizationErrorInfo errorInfo,
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

    private void logError(AuthorizationErrorInfo errorInfo) {
        log.error("[Authorization Error] {} : {}", errorInfo.errorCode(), errorInfo.message());
    }
}
