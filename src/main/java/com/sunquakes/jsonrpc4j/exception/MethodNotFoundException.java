package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class MethodNotFoundException extends RuntimeException {

    private int code;

    public MethodNotFoundException() {
        this(ErrorEnum.MethodNotFound.getText());
    }

    public MethodNotFoundException(String message) {
        super(message);
        this.code = ErrorEnum.MethodNotFound.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
