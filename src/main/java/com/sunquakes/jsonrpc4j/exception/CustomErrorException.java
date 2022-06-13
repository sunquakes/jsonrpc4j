package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class CustomErrorException extends RuntimeException {

    private int code;

    public CustomErrorException() {
        this(ErrorEnum.CustomError.getText());
    }

    public CustomErrorException(String message) {
        super(message);
        this.code = ErrorEnum.CustomError.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
