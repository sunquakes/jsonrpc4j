package com.sunquakes.jsonrpc4j.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotifyResponseDto {

    private String jsonrpc;

    private Object result;
}
