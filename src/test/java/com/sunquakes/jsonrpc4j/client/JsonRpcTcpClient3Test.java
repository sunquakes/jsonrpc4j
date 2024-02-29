package com.sunquakes.jsonrpc4j.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-tcp3.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcTcpClient3Test {

    @Resource
    private IJsonRpcTcpClient3 jsonRpcTcpClient3;

    @Test
    public void testHandler() {
        // test tcp handler
        // assertEquals(jsonRpcTcpClient3.add(1, 2), 3);
        // assertEquals(jsonRpcTcpClient3.add(3, 4), 7);
        // assertEquals(jsonRpcTcpClient3.add(5, 6), 11);
    }
}
