package com.sunquakes.jsonrpc4j.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcClientTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testHandler() throws IOException {
        // test http handler
        {
            IJsonRpcClient jsonRpcClient = applicationContext.getBean(IJsonRpcClient.class);
            assertEquals(jsonRpcClient.add(3, 4), 7);
        }
    }
}
