package com.sunquakes.jsonrpc4j.spring.boot.dto;

public class ArgsDto {

    private int a;

    private int b;

    private ArgsDto args;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public ArgsDto getArgs() {
        return args;
    }

    public void setArgs(ArgsDto args) {
        this.args = args;
    }
}