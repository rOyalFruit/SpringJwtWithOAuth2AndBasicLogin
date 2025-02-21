package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.base.ErrorCode;

public class TokenNotFoundException extends TokenException {
    public TokenNotFoundException() {
        super(ErrorCode.TOKEN_NOT_FOUND);
    }

    public TokenNotFoundException(String message) {
        super(ErrorCode.TOKEN_NOT_FOUND, message);
    }
}
