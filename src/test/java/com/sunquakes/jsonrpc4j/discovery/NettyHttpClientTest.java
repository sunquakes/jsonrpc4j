package com.sunquakes.jsonrpc4j.discovery;

import com.sun.net.httpserver.HttpServer;
import com.sunquakes.jsonrpc4j.client.NettyHttpClient;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NettyHttpClientTest {

    private HttpServer server;
    private NettyHttpClient client;
    private final AtomicReference<String[]> receivedRequest = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            receivedRequest.set(new String[]{
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().toString(),
                    requestBody,
                    exchange.getRequestHeaders().getFirst("Content-Length")
            });
            byte[] response = ("received:" + requestBody).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        client = new NettyHttpClient("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testGet() throws Exception {
        FullHttpResponse response = client.get("/hello?value=1");
        try {
            assertEquals(HttpResponseStatus.OK, response.status());
            assertEquals("received:", response.content().toString(CharsetUtil.UTF_8));
        } finally {
            response.release();
        }
        assertRequest("GET", "/hello?value=1", "", null);
    }

    @Test
    void testPostUsesUtf8ContentAndLength() throws Exception {
        String body = "{\"name\":\"小明\"}";
        FullHttpResponse response = client.post("/json", body);
        try {
            assertEquals(HttpResponseStatus.OK, response.status());
            assertEquals("received:" + body, response.content().toString(CharsetUtil.UTF_8));
        } finally {
            response.release();
        }
        assertRequest("POST", "/json", body, String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
    }

    @Test
    void testPut() throws Exception {
        FullHttpResponse response = client.put("/json", "更新");
        try {
            assertEquals("received:更新", response.content().toString(CharsetUtil.UTF_8));
        } finally {
            response.release();
        }
        assertRequest("PUT", "/json", "更新", "6");
    }

    private void assertRequest(String method, String uri, String body, String contentLength) {
        String[] request = receivedRequest.get();
        assertEquals(method, request[0]);
        assertEquals(uri, request[1]);
        assertEquals(body, request[2]);
        assertEquals(contentLength, request[3]);
    }
}
