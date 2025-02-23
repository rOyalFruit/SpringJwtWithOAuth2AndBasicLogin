package com.ll.backend.global.exception.auth;

import com.ll.backend.global.exception.base.ErrorCode;

public class LogoutException extends AuthException {

    public LogoutException() {
        super(ErrorCode.LOGOUT_PROCESSING_ERROR);
    }

    public LogoutException(String message) {
        super(ErrorCode.LOGOUT_PROCESSING_ERROR, message);
    }
}
