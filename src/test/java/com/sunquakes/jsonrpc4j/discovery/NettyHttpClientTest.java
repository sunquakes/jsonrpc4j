package com.sunquakes.jsonrpc4j.discovery;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NettyHttpClientTest {

    @Test
    void testGet() throws ExecutionException, InterruptedException {
        NettyHttpClient nettyHttpClient = mock(NettyHttpClient.class);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Hello world.", StandardCharsets.UTF_8));
        when(nettyHttpClient.get(anyString())).thenReturn(fullHttpResponse);
        assertEquals("Hello world.", nettyHttpClient.get("/").content().toString(CharsetUtil.UTF_8));
    }

    @Test
    void testPost() throws ExecutionException, InterruptedException {
        NettyHttpClient nettyHttpClient = mock(NettyHttpClient.class);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Hello world.", StandardCharsets.UTF_8));
        when(nettyHttpClient.post(anyString(), any())).thenReturn(fullHttpResponse);
        assertEquals("Hello world.", nettyHttpClient.post("/", null).content().toString(CharsetUtil.UTF_8));
    }

    @Test
    void testPut() throws ExecutionException, InterruptedException {
        NettyHttpClient nettyHttpClient = mock(NettyHttpClient.class);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Hello world.", StandardCharsets.UTF_8));
        when(nettyHttpClient.put(anyString(), any())).thenReturn(fullHttpResponse);
        assertEquals("Hello world.", nettyHttpClient.put("/", null).content().toString(CharsetUtil.UTF_8));
    }
}
