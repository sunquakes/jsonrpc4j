package com.sunquakes.jsonrpc4j;

public enum ProtocolEnum {

    Tcp("tcp"),
    Http("http"),
    Https("https");

    private String name;

    ProtocolEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}