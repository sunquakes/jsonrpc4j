package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-https.properties")
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcHttpsClientTest {

    @Autowired
    private IJsonRpcHttpsClient jsonRpcHttpsClient;

    @Test
    public void testRequest() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JSONObject params = new JSONObject();
        params.put("a", 1);
        params.put("b", 2);
        JSONObject request = new JSONObject();
        request.put("id", "1234567890");
        request.put("jsonrpc", "2.0");
        request.put("method", "JsonRpc/add");
        request.put("params", params);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        HttpPost httpPost = new HttpPost("https://localhost:3205");
        httpPost.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals(EntityUtils.toString(response.getEntity()), "{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}");
    }

    @Test
    public void testHandler() {
        // test https handler
        assertEquals(jsonRpcHttpsClient.add(1, 2), 3);
        assertEquals(jsonRpcHttpsClient.add(3, 4), 7);
        assertEquals(jsonRpcHttpsClient.add(5, 6), 11);
    }

    @Test
    public void testLongParams() {
        InputStream text1IS = this.getClass().getClassLoader().getResourceAsStream("text1.txt");
        String text1 = new BufferedReader(new InputStreamReader(text1IS)).lines().collect(Collectors.joining(System.lineSeparator()));
        InputStream text2IS = this.getClass().getClassLoader().getResourceAsStream("text2.txt");
        String text2 = new BufferedReader(new InputStreamReader(text2IS)).lines().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(text1 + text2, jsonRpcHttpsClient.splice(text1, text2));
    }
}
