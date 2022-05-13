package com.sunquakes.jsonrpc4j.spring.utils;

import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RequestUtilsTest {

    @Test
    public void testParseRequestMethod() throws MethodNotFoundException {
        String method = "JsonRpc/add";
        String[] arr = RequestUtils.parseMethod(method);
        assertEquals(arr[0], "JsonRpc");
        assertEquals(arr[1], "add");
    }

    @Test
    public void testParseRequestBody() throws InvalidRequestException, InvalidParamsException {
        // test RequestDto||NotifyRequestDto
        {
            String json = "{" +
                    "\"id\":\"1234567890\"" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "}";
            Object request = RequestUtils.parseRequestBody(json);
            assertSame(RequestDto.class, request.getClass());
            RequestDto requestDto = (RequestDto) request;
            assertEquals(requestDto.getId(), "1234567890");
            assertEquals(requestDto.getJsonrpc(), "2.0");

            // test parse params
            Object[] params = RequestUtils.parseParams(requestDto.getParams(), new String[]{"a", "b"});
            assertEquals(params[0], 1);
            assertEquals(params[1], 2);
        }

        // test RequestDto||NotifyRequestDto Array
        {
            String json = "[{" +
                    "\"id\":\"1234567890\"" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "},{" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":[1, 2]" +
                    "}]";
            Object request = RequestUtils.parseRequestBody(json);
            assertSame(ArrayList.class, request.getClass());
            List<Object> requestDtoList = (List<Object>) request;
            RequestDto requestDto = (RequestDto) requestDtoList.get(0);
            assertEquals(requestDto.getId(), "1234567890");
            assertEquals(requestDto.getJsonrpc(), "2.0");
            NotifyRequestDto notifyRequestDto = (NotifyRequestDto) requestDtoList.get(1);
            assertEquals(notifyRequestDto.getJsonrpc(), "2.0");

            // test parse params
            Object[] params = RequestUtils.parseParams(requestDto.getParams(), new String[]{"a", "b"});
            assertEquals(params[0], 1);
            assertEquals(params[1], 2);

        }
    }
}
