package com.sunquakes.jsonrpc4j.spring;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcHttpServerTest {

    @Test
    public void testMethod() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpget = new HttpPost("http://localhost:3200");
        HttpResponse response = httpclient.execute(httpget);

        assertEquals(EntityUtils.toString(response.getEntity()), "{\"res\":\"7\"}");
    }
}
