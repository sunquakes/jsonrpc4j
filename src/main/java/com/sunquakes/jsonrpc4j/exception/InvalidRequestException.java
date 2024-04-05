package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class InvalidRequestException extends JsonRpcException {

    private final int code;

    public InvalidRequestException() {
        this(ErrorEnum.INVALID_REQUEST.getText());
    }

    public InvalidRequestException(String message) {
        super(message);
        this.code = ErrorEnum.INVALID_REQUEST.getCode();
    }

    public int getCode() {
        return code;
    }
}
