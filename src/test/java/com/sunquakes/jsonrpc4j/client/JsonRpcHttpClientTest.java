package com.sunquakes.jsonrpc4j.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcHttpClientTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testHandler() {
        // test http handler
        {
            IJsonRpcHttpClient jsonRpcHttpClient = applicationContext.getBean(IJsonRpcHttpClient.class);
            assertEquals(jsonRpcHttpClient.add(1, 2), 3);
            assertEquals(jsonRpcHttpClient.add(3, 4), 7);
            assertEquals(jsonRpcHttpClient.add(5, 6), 11);
        }
    }
}
