package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InvalidParamsException extends JsonRpcException {

    private final int code;

    public InvalidParamsException() {
        this(ErrorEnum.INVALID_PARAMS.getText());
    }

    public InvalidParamsException(String message) {
        super(message);
        this.code = ErrorEnum.INVALID_PARAMS.getCode();
    }

    public int getCode() {
        return code;
    }
}
