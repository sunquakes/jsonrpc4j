package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHandler;
import com.sunquakes.jsonrpc4j.dto.NotifyResponseDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

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
    }
}
