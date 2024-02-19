package com.sunquakes.jsonrpc4j.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-tcp.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcTcpClientTest {

    @Autowired
    private IJsonRpcTcpClient jsonRpcTcpClient;

    private String text1;
    private String text2;

    @Before
    public void beforeTest() throws UnsupportedEncodingException {
        InputStream text1IS = this.getClass().getClassLoader().getResourceAsStream("text1.txt");
        text1 = new BufferedReader(new InputStreamReader(text1IS, "UTF-8")).lines().collect(Collectors.joining(System.lineSeparator()));
        InputStream text2IS = this.getClass().getClassLoader().getResourceAsStream("text2.txt");
        text2 = new BufferedReader(new InputStreamReader(text2IS, "UTF-8")).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    public void testHandler() {
        // test tcp handler
        assertEquals(jsonRpcTcpClient.add(1, 2), 3);
        assertEquals(jsonRpcTcpClient.add(3, 4), 7);
        assertEquals(jsonRpcTcpClient.add(5, 6), 11);
    }

    @Test
    public void testLongParams() {
        assertEquals(text1 + text2, jsonRpcTcpClient.splice(text1, text2));
    }
}
