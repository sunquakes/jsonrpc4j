package com.sunquakes.jsonrpc4j.dto;

import lombok.Data;

@Data
public class ErrorNotifyResponseDto {

    private String jsonrpc;

    private String error;
}
