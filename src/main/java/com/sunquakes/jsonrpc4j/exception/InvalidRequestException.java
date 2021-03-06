package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InvalidRequestException extends RuntimeException {

    private int code;

    public InvalidRequestException() {
        this(ErrorEnum.InvalidRequest.getText());
    }

    public InvalidRequestException(String message) {
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
