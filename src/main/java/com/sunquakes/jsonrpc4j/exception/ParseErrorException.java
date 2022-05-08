package com.sunquakes.jsonrpc4j.exception;

import com.sunquakes.jsonrpc4j.ErrorEnum;

public class ParseErrorException extends Exception {

    private int code;

    public ParseErrorException() {
        super(ErrorEnum.ParseError.getText());
    }

    ParseErrorException(String message) {
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
