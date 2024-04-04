package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class ParseErrorException extends JsonRpcException {

    private final int code;

    public ParseErrorException() {
        this(ErrorEnum.PARSE_ERROR.getText());
    }

    public ParseErrorException(String message) {
        super(message);
        this.code = ErrorEnum.PARSE_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }
}
