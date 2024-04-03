package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class MethodNotFoundException extends RuntimeException {

    private final int code;

    public MethodNotFoundException() {
        this(ErrorEnum.METHOD_NOT_FOUND.getText());
    }

    public MethodNotFoundException(String message) {
        super(message);
        this.code = ErrorEnum.METHOD_NOT_FOUND.getCode();
    }

    public int getCode() {
        return code;
    }
}
