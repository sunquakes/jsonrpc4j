package com.sunquakes.jsonrpc4j.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-tcp2.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcTcpClient2Test {

    @Autowired
    private IJsonRpcTcpClient2 jsonRpcTcpClient2;

    @Test
    public void testHandler() {
        // test tcp handler
        assertEquals(jsonRpcTcpClient2.add(1, 2), 3);
        assertEquals(jsonRpcTcpClient2.add(3, 4), 7);
        assertEquals(jsonRpcTcpClient2.add(5, 6), 11);
    }

    @Test
    public void testLongParams() {
        InputStream text1IS = this.getClass().getClassLoader().getResourceAsStream("text1.txt");
        String text1 = new BufferedReader(new InputStreamReader(text1IS)).lines().collect(Collectors.joining(System.lineSeparator()));
        InputStream text2IS = this.getClass().getClassLoader().getResourceAsStream("text2.txt");
        String text2 = new BufferedReader(new InputStreamReader(text2IS)).lines().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(text1 + text2, jsonRpcTcpClient2.splice(text1, text2));
    }
}
