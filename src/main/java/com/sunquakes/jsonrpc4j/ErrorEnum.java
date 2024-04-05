package com.sunquakes.jsonrpc4j;

import com.sunquakes.jsonrpc4j.exception.*;

public enum ErrorEnum {

    PARSE_ERROR(-32700, "Parse error"),
    INVALID_REQUEST(-32600, "Invalid request"),
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    INTERNAL_ERROR(-32603, "Internal error"),
    CUSTOM_ERROR(-32000, null);

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

    public static JsonRpcException getException(int code, String message) {
        JsonRpcException e;
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
                e = new JsonRpcException(message);
        }
        return e;
    }
}
