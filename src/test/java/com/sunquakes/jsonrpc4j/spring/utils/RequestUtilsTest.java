package com.sunquakes.jsonrpc4j.spring.utils;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RequestUtilsTest {

    @Test
    public void testRequestBody() {
//        test RequestDto
        {
            String json = "{" +
                    "\"id\":\"1234567890\"" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "}";
            try {
                Object request = RequestUtils.parseRequestBody(json);
                assertSame(RequestDto.class, request.getClass());
                RequestDto requestDto = (RequestDto) request;
                assertEquals(requestDto.getId(), "1234567890");
                assertEquals(requestDto.getJsonrpc(), "2.0");
            } catch (InvalidRequestException e) {
                e.printStackTrace();
            }
        }
    }
}
