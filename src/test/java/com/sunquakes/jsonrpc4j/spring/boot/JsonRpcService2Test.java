package com.sunquakes.jsonrpc4j.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

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
    private String protocol;

    @Value("${jsonrpc.server.port}")
    private int port;

    @Autowired
    private IJsonRpcClient jsonRpcClient;

    @Test
    public void testGetConfiguration() {
        assertEquals(protocol, "tcp");
        assertEquals(port, 3202);
    }

    @Test
    public void testRequest() throws IOException, InterruptedException {
        // test request
        {
            assertEquals(jsonRpcClient.add(1, 2), 7);
            assertEquals(jsonRpcClient.add(3, 4), 7);
            assertEquals(jsonRpcClient.add(5, 2), 7);
        }
    }
}
