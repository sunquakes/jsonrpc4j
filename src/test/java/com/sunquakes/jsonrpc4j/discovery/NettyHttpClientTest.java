package com.sunquakes.jsonrpc4j.discovery;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NettyHttpClientTest {

    @Test
    void testGet() {
        NettyHttpClient httpClient = new NettyHttpClient("https://www.github.com");
        try {
            System.out.println(httpClient.get("/"));
            assertNotNull(httpClient.get("/"));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
