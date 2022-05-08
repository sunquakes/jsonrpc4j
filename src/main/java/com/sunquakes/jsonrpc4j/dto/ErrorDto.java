package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class ErrorDto {

    private int code;

    private String message;

    private Object data;
}
