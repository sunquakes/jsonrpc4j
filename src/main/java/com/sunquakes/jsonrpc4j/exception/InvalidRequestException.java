package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InvalidRequestException extends Exception {

    private int code;

    public InvalidRequestException() {
        super(ErrorEnum.InvalidRequest.getText());
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
