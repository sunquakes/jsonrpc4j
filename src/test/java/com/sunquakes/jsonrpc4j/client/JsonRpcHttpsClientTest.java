package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
    void testRequest() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        @Data
        @AllArgsConstructor
        class Params {
            int a;
            int b;
        }
        Params params = new Params(1, 2);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "JsonRpc/add", params);
        String request = JSONUtils.toString(requestDto);

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
        httpPost.setEntity(new StringEntity(request, ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals("{\"id\":\"1234567890\",\"jsonrpc\":\"2.0\",\"result\":3}", EntityUtils.toString(response.getEntity()));
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
