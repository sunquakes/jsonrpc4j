package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.dto.NotifyResponseDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcServerHandlerTest {

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
            com.sunquakes.jsonrpc4j.server.JsonRpcServerHandler jsonRpcServerHandler = applicationContext.getBean(com.sunquakes.jsonrpc4j.server.JsonRpcServerHandler.class);
            ResponseDto responseDto = (ResponseDto) jsonRpcServerHandler.handle(json);
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
            JsonRpcServerHandler jsonRpcServerHandler = applicationContext.getBean(JsonRpcServerHandler.class);
            NotifyResponseDto notifyResponseDto = (NotifyResponseDto) jsonRpcServerHandler.handle(json);
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
            com.sunquakes.jsonrpc4j.server.JsonRpcServerHandler jsonRpcServerHandler = applicationContext.getBean(com.sunquakes.jsonrpc4j.server.JsonRpcServerHandler.class);
            Object res = jsonRpcServerHandler.handle(json);
            assertTrue(res instanceof ArrayList);

            List<Object> list = (List<Object>) res;
            assertTrue(list.get(0) instanceof ResponseDto);
            assertTrue(list.get(1) instanceof NotifyResponseDto);
        }
    }
}
