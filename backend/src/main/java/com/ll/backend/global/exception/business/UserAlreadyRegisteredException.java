package com.ll.backend.global.exception.business;

import com.ll.backend.global.exception.base.ErrorCode;

public class UserAlreadyRegisteredException extends BusinessException {
    public UserAlreadyRegisteredException() {
        super(ErrorCode.USER_ALREADY_REGISTERED);
    }

    public UserAlreadyRegisteredException(String message) {
        super(ErrorCode.USER_ALREADY_REGISTERED, message);
    }
}
