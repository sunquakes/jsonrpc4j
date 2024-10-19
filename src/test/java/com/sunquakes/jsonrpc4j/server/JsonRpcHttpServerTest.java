package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.client.NettyHttpClient;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcHttpServerTest {

    @Data
    @AllArgsConstructor
    static class Params {
        int a;
        int b;
    }

    @Test
    void testHandle() throws ExecutionException, InterruptedException {
        Params params = new Params(1, 2);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "JsonRpc/add", params);
        String request = JSONUtils.toString(requestDto);

        NettyHttpClient httpClient = new NettyHttpClient("http://localhost:3200");
        FullHttpResponse res = httpClient.post("", request);
        ByteBuf buf = res.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}", body);
    }

    @Test
    void testMethod() throws ExecutionException, InterruptedException {
        Params params = new Params(3, 4);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "json_rpc/add", params);
        String request = JSONUtils.toString(requestDto);

        NettyHttpClient httpClient = new NettyHttpClient("http://localhost:3200");
        FullHttpResponse res = httpClient.post("", request);
        ByteBuf buf = res.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":7}", body);
    }
}
