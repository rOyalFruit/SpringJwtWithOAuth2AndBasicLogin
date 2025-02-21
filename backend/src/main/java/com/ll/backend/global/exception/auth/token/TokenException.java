package com.ll.backend.global.exception.auth.token;

import com.ll.backend.global.exception.auth.AuthException;
import com.ll.backend.global.exception.base.ErrorCode;

/**
 * TokenException: 토큰 관련 예외의 기본 클래스
 * **/
public class TokenException extends AuthException {
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
