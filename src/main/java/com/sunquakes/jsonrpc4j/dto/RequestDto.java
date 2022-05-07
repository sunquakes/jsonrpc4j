package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class RequestDto {

    private String id;

    private String jsonrpc;

    private String method;

    private Object params;
}
