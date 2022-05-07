package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class NotifyResponseDto {

    private String jsonrpc;

    private Object result;
}
