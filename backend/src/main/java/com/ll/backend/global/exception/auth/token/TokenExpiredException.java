package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.base.ErrorCode;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }

    public TokenExpiredException(String message) {
        super(ErrorCode.EXPIRED_TOKEN, message);
    }
}
