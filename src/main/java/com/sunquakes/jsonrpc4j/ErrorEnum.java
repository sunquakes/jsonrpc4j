package com.sunquakes.jsonrpc4j;

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
}
