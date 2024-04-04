package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class CustomErrorException extends JsonRpcException {

    private final int code;

    public CustomErrorException() {
        this(ErrorEnum.CUSTOM_ERROR.getText());
    }

    public CustomErrorException(String message) {
        super(message);
        this.code = ErrorEnum.CUSTOM_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }
}
