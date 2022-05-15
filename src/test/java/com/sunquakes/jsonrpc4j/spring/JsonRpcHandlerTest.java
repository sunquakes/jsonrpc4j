package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.server.JsonRpcHandler;
import com.sunquakes.jsonrpc4j.dto.NotifyResponseDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcHandlerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testHandle() {
        // test ResponseDto
        {
            String json = "{" +
                    "\"id\":\"1234567890\"" +
                    "\"jsonrpc\":2.0" +
                    "\"method\":\"JsonRpc/add\"" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "}";
            JsonRpcHandler jsonRpcHandler = applicationContext.getBean(JsonRpcHandler.class);
            ResponseDto responseDto = (ResponseDto) jsonRpcHandler.handle(json);
            assertEquals(responseDto.getId(), "1234567890");
            assertEquals(responseDto.getJsonrpc(), "2.0");
            assertEquals(responseDto.getResult(), 3);
        }

        // test NotifyResponseDto
        {
            String json = "{" +
                    "\"jsonrpc\":2.0" +
                    "\"method\":\"JsonRpc/add\"" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "}";
            JsonRpcHandler jsonRpcHandler = applicationContext.getBean(JsonRpcHandler.class);
            NotifyResponseDto notifyResponseDto = (NotifyResponseDto) jsonRpcHandler.handle(json);
            assertEquals(notifyResponseDto.getJsonrpc(), "2.0");
            assertEquals(notifyResponseDto.getResult(), 3);
        }

        // test RequestDto||NotifyRequestDto Array
        {
            String json = "[{" +
                    "\"id\":\"1234567890\"" +
                    "\"method\":\"JsonRpc/add\"" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":{\"a\":1,\"b\":2}" +
                    "},{" +
                    "\"method\":\"JsonRpc/sub\"" +
                    "\"jsonrpc\":2.0" +
                    "\"params\":[2, 1]" +
                    "}]";
            JsonRpcHandler jsonRpcHandler = applicationContext.getBean(JsonRpcHandler.class);
            Object res = jsonRpcHandler.handle(json);
            assertTrue(res instanceof ArrayList);

            List<Object> list = (List<Object>) res;
            assertTrue(list.get(0) instanceof ResponseDto);
            assertTrue(list.get(1) instanceof NotifyResponseDto);
        }
    }
}
