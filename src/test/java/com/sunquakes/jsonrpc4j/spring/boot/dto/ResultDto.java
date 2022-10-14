package com.sunquakes.jsonrpc4j.spring.boot.dto;

public class ResultDto {

    private int c;

    private ResultDto result;

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public ResultDto getResult() {
        return result;
    }

    public void setResult(ResultDto result) {
        this.result = result;
    }
}