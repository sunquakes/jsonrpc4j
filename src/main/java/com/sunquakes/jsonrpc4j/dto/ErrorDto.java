package com.sunquakes.jsonrpc4j.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDto {

    private int code;

    private String message;

    private Object data;
}
