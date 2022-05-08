package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class MethodNotFoundException extends Exception {

    private int code;

    public MethodNotFoundException() {
        super(ErrorEnum.InvalidRequest.getText());
    }

    public MethodNotFoundException(String message) {
        super(message);
        this.code = ErrorEnum.InvalidRequest.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
