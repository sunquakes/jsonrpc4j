package com.sunquakes.jsonrpc4j.utils;

import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.*;
import lombok.experimental.UtilityClass;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@UtilityClass
public class ResponseUtils {

    private String JSONRPC = RequestUtils.JSONRPC;

    public Object error(String id, int code) {
        return error(id, code, null);
    }

    public Object error(String id, int code, String message) {
        if (id == null) {
            ErrorDto errorDto = new ErrorDto(code, ErrorEnum.getTextByCode(code), message);
            return new ErrorNotifyResponseDto(JSONRPC, errorDto);
        } else {
            ErrorDto errorDto = new ErrorDto(code, ErrorEnum.getTextByCode(code), message);
            return new ErrorResponseDto(id, JSONRPC, errorDto);
        }
    }

    public Object success(String id, Object data) {
        if (id == null) {
            return new NotifyResponseDto(JSONRPC, data);
        } else {
            return new ResponseDto(id, JSONRPC, data);
        }
    }
}
