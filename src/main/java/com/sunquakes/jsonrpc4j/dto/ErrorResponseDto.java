package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class ErrorResponseDto {

    private String id;

    private String jsonrpc;

    private ErrorDto error;
}
