package com.sunquakes.jsonrpc4j.utils;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorNotifyResponseDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseUtils {

    private String JSONRPC = "2.0";

    public ErrorResponseDto error(String id, int code) {
        ErrorDto errorDto = new ErrorDto(code, ErrorEnum.getTextByCode(code), null);
        return new ErrorResponseDto(id, JSONRPC, errorDto);
    }

    public ErrorResponseDto error(String id, int code, String message) {
        ErrorDto errorDto = new ErrorDto(code, message, null);
        return new ErrorResponseDto(id, JSONRPC, errorDto);
    }

    public ErrorNotifyResponseDto errorNotify(String id, int code) {
        ErrorDto errorDto = new ErrorDto(code, ErrorEnum.getTextByCode(code), null);
        return new ErrorNotifyResponseDto(JSONRPC, errorDto);
    }

    public ErrorNotifyResponseDto errorNotify(String id, int code, String message) {
        ErrorDto errorDto = new ErrorDto(code, message, null);
        return new ErrorNotifyResponseDto(JSONRPC, errorDto);
    }
}
