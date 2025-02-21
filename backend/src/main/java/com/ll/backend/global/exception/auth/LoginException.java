package com.ll.backend.global.exception.auth;

import com.ll.backend.global.exception.base.ErrorCode;

public class LoginException extends AuthException {
    public LoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }

    public LoginException(String message) {
        super(ErrorCode.INVALID_LOGIN, message);
    }
}
