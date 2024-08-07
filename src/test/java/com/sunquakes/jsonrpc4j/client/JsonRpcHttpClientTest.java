package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcHttpClientTest {

    @Resource
    private IJsonRpcHttpClient jsonRpcHttpClient;

    private String text1;
    private String text2;

    @BeforeEach
    public void beforeTest() throws UnsupportedEncodingException {
        InputStream text1IS = this.getClass().getClassLoader().getResourceAsStream("text1.txt");
        text1 = new BufferedReader(new InputStreamReader(text1IS, "UTF-8")).lines().collect(Collectors.joining(System.lineSeparator()));
        InputStream text2IS = this.getClass().getClassLoader().getResourceAsStream("text2.txt");
        text2 = new BufferedReader(new InputStreamReader(text2IS, "UTF-8")).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    void testException() {
        try {
            jsonRpcHttpClient.methodNotFount();
        } catch (Throwable e) {
            assertEquals(MethodNotFoundException.class, e.getClass());
            assertEquals("Method not found", e.getMessage());
        }
    }

    @Test
    void testHandler() {
        // test http handler
        assertEquals(3, jsonRpcHttpClient.add(1, 2));
        assertEquals(7, jsonRpcHttpClient.add(3, 4));
        assertEquals(11, jsonRpcHttpClient.add(5, 6));
    }

    @Test
    void testLongParams() {
        assertEquals(text1 + text2, jsonRpcHttpClient.splice(text1, text2));
    }
}
