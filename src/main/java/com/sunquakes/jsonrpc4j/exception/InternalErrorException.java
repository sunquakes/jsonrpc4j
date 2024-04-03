package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InternalErrorException extends RuntimeException {

    private final int code;

    public InternalErrorException() {
        this(ErrorEnum.INTERNAL_ERROR.getText());
    }

    public InternalErrorException(String message) {
        super(message);
        this.code = ErrorEnum.INTERNAL_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }
}
