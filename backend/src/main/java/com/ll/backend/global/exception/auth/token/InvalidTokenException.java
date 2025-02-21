package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.base.ErrorCode;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
