package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.base.ErrorCode;

public class ExpiredTokenException extends TokenException {
    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }

    public ExpiredTokenException(String message) {
        super(ErrorCode.EXPIRED_TOKEN, message);
    }
}
