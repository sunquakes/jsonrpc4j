package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSONObject;
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

    @Test
    void testHandle() throws IOException {
        JSONObject params = new JSONObject();
        params.put("a", 1);
        params.put("b", 2);
        JSONObject request = new JSONObject();
        request.put("id", "1234567890");
        request.put("jsonrpc", "2.0");
        request.put("method", "JsonRpc/add");
        request.put("params", params);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:3200");
        httpPost.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}", EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testMethod() throws IOException {
        JSONObject params = new JSONObject();
        params.put("a", 3);
        params.put("b", 4);
        JSONObject request = new JSONObject();
        request.put("id", "1234567890");
        request.put("jsonrpc", "2.0");
        request.put("method", "json_rpc/add");
        request.put("params", params);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:3200");
        httpPost.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":7}", EntityUtils.toString(response.getEntity()));
    }
}
