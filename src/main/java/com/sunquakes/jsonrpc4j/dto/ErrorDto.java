package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class ErrorDto {

    private String code;

    private String message;

    private Object data;
}
