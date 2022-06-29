package com.sunquakes.jsonrpc4j.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-tcp2.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcTcpClient2Test {

    @Autowired
    private IJsonRpcTcpClient2 jsonRpcTcpClient2;

    private String text1;
    private String text2;

    @Before
    public void beforeTest() {
        InputStream text1IS = this.getClass().getClassLoader().getResourceAsStream("text1.txt");
        text1 = new BufferedReader(new InputStreamReader(text1IS)).lines().collect(Collectors.joining(System.lineSeparator()));
        InputStream text2IS = this.getClass().getClassLoader().getResourceAsStream("text2.txt");
        text2 = new BufferedReader(new InputStreamReader(text2IS)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    public void testHandler() {
        // test tcp handler
        assertEquals(jsonRpcTcpClient2.add(1, 2), 3);
        assertEquals(jsonRpcTcpClient2.add(3, 4), 7);
        assertEquals(jsonRpcTcpClient2.add(5, 6), 11);
    }

    @Test
    public void testLongParams() {
        assertEquals(text1 + text2, jsonRpcTcpClient2.splice(text1, text2));
    }

    @Test
    public void testMultithreading() throws InterruptedException {
        int co = 10;
        int total = 100;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        ExecutorService pool = Executors.newFixedThreadPool(co);
        for (int i = 0; i < total; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        assertEquals(3, jsonRpcTcpClient2.add(1, 2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        pool.shutdown();
    }
}
