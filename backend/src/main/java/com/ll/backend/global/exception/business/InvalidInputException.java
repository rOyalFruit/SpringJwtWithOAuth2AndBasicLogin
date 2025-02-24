package com.ll.backend.global.exception.business;

import com.ll.backend.global.exception.base.ErrorCode;

public class InvalidInputException extends BusinessException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }

    public InvalidInputException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}