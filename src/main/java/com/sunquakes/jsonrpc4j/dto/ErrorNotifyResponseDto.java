package com.sunquakes.jsonrpc4j.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorNotifyResponseDto {

    private String jsonrpc;

    private ErrorDto error;
}
