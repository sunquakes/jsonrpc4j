package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcHttpServerTest {

    @Data
    @AllArgsConstructor
    class Params {
        int a;
        int b;
    }

    @Test
    void testHandle() throws IOException {
        Params params = new Params(1, 2);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "JsonRpc/add", params);
        String request = JSONUtils.toString(requestDto);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:3200");
        httpPost.setEntity(new StringEntity(request, ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}", EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testMethod() throws IOException {
        Params params = new Params(3, 4);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "json_rpc/add", params);
        String request = JSONUtils.toString(requestDto);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:3200");
        httpPost.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":7}", EntityUtils.toString(response.getEntity()));
    }
}
