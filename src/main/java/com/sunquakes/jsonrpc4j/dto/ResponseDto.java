package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class ResponseDto {

    private String id;

    private String jsonrpc;

    private Object result;
}
