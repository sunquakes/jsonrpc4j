package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHttpServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class JsonRpcHttpServerTest {

    private JsonRpcHttpServer jsonRpcHttpServer;

    @Before
    public void setup() throws IOException {
    }

    @Test
    public void testMethod() throws IOException {
        jsonRpcHttpServer = new JsonRpcHttpServer();
        jsonRpcHttpServer.start();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpget = new HttpPost("http://localhost:3200");
        HttpResponse response = httpclient.execute(httpget);

        assertEquals(EntityUtils.toString(response.getEntity()), "test");
    }
}
