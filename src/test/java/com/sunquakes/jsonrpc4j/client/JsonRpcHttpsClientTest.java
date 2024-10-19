package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-https.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcHttpsClientTest {

    @Resource
    private IJsonRpcHttpsClient jsonRpcHttpsClient;

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
    void testRequest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ExecutionException, InterruptedException {
        @Data
        @AllArgsConstructor
        class Params {
            int a;
            int b;
        }
        Params params = new Params(1, 2);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "JsonRpc/add", params);
        String request = JSONUtils.toString(requestDto);

        NettyHttpClient httpClient = new NettyHttpClient("https://localhost:3205");
        FullHttpResponse res = httpClient.post("", request);
        ByteBuf buf = res.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}", body);
    }

    @Test
    void testHandler() {
        // test https handler
        assertEquals(3, jsonRpcHttpsClient.add(1, 2));
        assertEquals(7, jsonRpcHttpsClient.add(3, 4));
        assertEquals(11, jsonRpcHttpsClient.add(5, 6));
    }

    @Test
    void testLongParams() {
        assertEquals(text1 + text2, jsonRpcHttpsClient.splice(text1, text2));
    }
}
