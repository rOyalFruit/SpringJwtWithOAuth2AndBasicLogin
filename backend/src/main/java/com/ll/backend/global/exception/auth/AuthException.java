package com.ll.backend.global.exception.auth;

import com.ll.backend.global.exception.base.BaseException;
import com.ll.backend.global.exception.base.ErrorCode;

/**
 * 인증 관련 모든 예외의 기본 클래스
 * **/
public class AuthException extends BaseException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
