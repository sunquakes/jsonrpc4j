package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHandler;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcHandlerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testHandle() {
        String json = "{" +
                "\"id\":\"1234567890\"" +
                "\"jsonrpc\":2.0" +
                "\"method\":\"JsonRpc/add\"" +
                "\"params\":{\"a\":1,\"b\":2}" +
                "}";
        JsonRpcHandler jsonRpcHandler = applicationContext.getBean(JsonRpcHandler.class);
        ResponseDto responseDto = (ResponseDto) jsonRpcHandler.handle(json);
        System.out.println(responseDto);
    }
}
