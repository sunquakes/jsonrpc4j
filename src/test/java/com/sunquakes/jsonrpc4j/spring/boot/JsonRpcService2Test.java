package com.sunquakes.jsonrpc4j.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring.boot
 * @Author: Robert
 * @CreateTime: 2022/5/30 12:48 PM
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JsonRpcApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JsonRpcService2Test {

    @Value("${jsonrpc.server.protocol}")
    private final String protocol = null;

    @Test
    public void testGetConfiguration() {
        assertEquals(protocol, "tcp");
    }
}
