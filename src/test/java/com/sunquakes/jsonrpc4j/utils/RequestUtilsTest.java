package com.sunquakes.jsonrpc4j.utils;

import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestUtilsTest {

    @Test
    void testParseRequestMethod() throws MethodNotFoundException {
        String method = "JsonRpc/add";
        String[] arr = RequestUtils.parseMethod(method);
        assertEquals("JsonRpc", arr[0]);
        assertEquals("add", arr[1]);
    }

    @Test
    void testParseRequestBody() throws InvalidRequestException, InvalidParamsException {
        // test RequestDto||NotifyRequestDto
        {
            String json = "{" +
                    "\"id\":\"1234567890\"," +
                    "\"jsonrpc\":2.0," +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "}";
            Object request = RequestUtils.parseRequestBody(json);
            assertSame(RequestDto.class, request.getClass());
            RequestDto requestDto = (RequestDto) request;
            assertEquals("1234567890", requestDto.getId());
            assertEquals("2.0", requestDto.getJsonrpc());

            // test parse params
            Object[] params = RequestUtils.parseParams(requestDto.getParams(), new String[]{"a", "b"});
            assertEquals(1, params[0]);
            assertEquals(2, params[1]);
        }

        // test RequestDto||NotifyRequestDto Array
        {
            String json = "[{" +
                    "\"id\":\"1234567890\"," +
                    "\"jsonrpc\":2.0," +
                    "\"params\":{\"a\":1,\"b\":2}," +
                    "},{" +
                    "\"jsonrpc\":2.0," +
                    "\"params\":[1, 2]" +
                    "}]";
            Object request = RequestUtils.parseRequestBody(json);
            assertSame(ArrayList.class, request.getClass());
            assertInstanceOf(List.class, request);
            List<Object> requestDtoList = (List<Object>) request;
            RequestDto requestDto = (RequestDto) requestDtoList.get(0);
            assertEquals("1234567890", requestDto.getId());
            assertEquals("2.0", requestDto.getJsonrpc());
            NotifyRequestDto notifyRequestDto = (NotifyRequestDto) requestDtoList.get(1);
            assertEquals("2.0", notifyRequestDto.getJsonrpc());

            // test parse params
            Object[] params = RequestUtils.parseParams(requestDto.getParams(), new String[]{"a", "b"});
            assertEquals(1, params[0]);
            assertEquals(2, params[1]);

        }
    }
}
