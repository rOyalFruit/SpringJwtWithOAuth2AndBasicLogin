package com.ll.backend.global.exception.business;

import com.ll.backend.global.exception.base.BaseException;
import com.ll.backend.global.exception.base.ErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
