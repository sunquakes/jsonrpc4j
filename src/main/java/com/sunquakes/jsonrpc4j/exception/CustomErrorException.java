package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class CustomErrorException extends Exception {

    private int code;

    public CustomErrorException() {
        super(ErrorEnum.CustomError.getText());
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
