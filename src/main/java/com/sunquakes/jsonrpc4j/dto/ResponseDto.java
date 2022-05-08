package com.sunquakes.jsonrpc4j.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto {

    private String id;

    private String jsonrpc;

    private Object result;
}
