package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.base.ErrorCode;

public class TokenGenerationException extends TokenException {
    public TokenGenerationException() {
        super(ErrorCode.TOKEN_GENERATION_FAILED);
    }

    public TokenGenerationException(String message) {
        super(ErrorCode.TOKEN_GENERATION_FAILED, message);
    }
}