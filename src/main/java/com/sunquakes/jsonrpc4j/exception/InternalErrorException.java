package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InternalErrorException extends Exception {

    private int code;

    public InternalErrorException() {
        super(ErrorEnum.InternalError.getText());
    }

    public InternalErrorException(String message) {
        super(message);
        this.code = ErrorEnum.InternalError.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
