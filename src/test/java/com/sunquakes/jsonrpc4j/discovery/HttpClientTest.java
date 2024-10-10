package com.sunquakes.jsonrpc4j.discovery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpClientTest {

    @Test
    void testGet() {
        HttpClient httpClient = new HttpClient("https://www.github.com");
        assertNotNull(httpClient.get("/"));
    }
}
