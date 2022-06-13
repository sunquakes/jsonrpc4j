package com.sunquakes.jsonrpc4j;

import com.sunquakes.jsonrpc4j.exception.*;

public enum ErrorEnum {

    ParseError(-32700, "Parse error"),
    InvalidRequest(-32600, "Invalid request"),
    MethodNotFound(-32601, "Method not found"),
    InvalidParams(-32602, "Invalid params"),
    InternalError(-32603, "Internal error"),
    CustomError(-32000, null);

    private int code;

    private String text;

    ErrorEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static String getTextByCode(int code) {
        for (ErrorEnum errorEnum : ErrorEnum.values()) {
            if (errorEnum.getCode() == code) {
                return errorEnum.getText();
            }
        }
        return null;
    }

    public static Exception getException(int code, String message) {
        Exception e;
        switch (code) {
            case -32700:
                e = new ParseErrorException(message);
                break;
            case -32600:
                e = new InvalidRequestException(message);
                break;
            case -32601:
                e = new MethodNotFoundException(message);
                break;
            case -32602:
                e = new InvalidParamsException(message);
                break;
            case -32603:
                e = new InternalErrorException(message);
                break;
            case -32000:
                e = new CustomErrorException(message);
                break;
            default:
                e = new Exception(message);
        }
        return e;
    }
}
