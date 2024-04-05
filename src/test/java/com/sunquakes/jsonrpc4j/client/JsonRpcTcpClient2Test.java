package com.sunquakes.jsonrpc4j.client;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-tcp2.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcTcpClient2Test {

    @Resource
    private IJsonRpcTcpClient2 jsonRpcTcpClient2;

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
    void testHandler() {
        // test tcp handler
        assertEquals(3, jsonRpcTcpClient2.add(1, 2));
        assertEquals(7, jsonRpcTcpClient2.add(3, 4));
        assertEquals(11, jsonRpcTcpClient2.add(5, 6));
    }

    @Test
    void testLongParams() {
        assertEquals(text1 + text2, jsonRpcTcpClient2.splice(text1, text2));
    }

    @Test
    void testMultithreading() throws InterruptedException {
        int co = 10;
        int total = 100;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        ExecutorService pool = Executors.newFixedThreadPool(co);
        for (int i = 0; i < total; i++) {
            pool.submit(() -> {
                try {
                    assertEquals(3, jsonRpcTcpClient2.add(1, 2));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        pool.shutdown();
    }
}
