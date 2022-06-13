package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InvalidParamsException extends RuntimeException {

    private int code;

    public InvalidParamsException() {
        this(ErrorEnum.InvalidParams.getText());
    }

    public InvalidParamsException(String message) {
        super(message);
        this.code = ErrorEnum.InvalidParams.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
