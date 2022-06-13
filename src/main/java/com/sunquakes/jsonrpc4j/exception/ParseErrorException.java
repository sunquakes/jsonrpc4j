package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class ParseErrorException extends RuntimeException {

    private int code;

    public ParseErrorException() {
        this(ErrorEnum.ParseError.getText());
    }

    public ParseErrorException(String message) {
        super(message);
        this.code = ErrorEnum.ParseError.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getCode() {
        return code;
    }
}
