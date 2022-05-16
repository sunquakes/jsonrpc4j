package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcTcpServerTest {

    @Test
    public void testHandle() throws IOException {
        JSONObject params = new JSONObject();
        params.put("a", 1);
        params.put("b", 2);
        JSONObject request = new JSONObject();
        request.put("id", "1234567890");
        request.put("jsonrpc", "2.0");
        request.put("method", "JsonRpc/add");
        request.put("params", params);

        try {
            Socket s = new Socket("localhost", 3201); // localhost 127.0.0.1 本机地址
            // 包装输入输出流
            OutputStream os = s.getOutputStream();
            os.write((request + "\r\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.write((request + "\r\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            StringBuffer sb = new StringBuffer();
            byte[] buffer = new byte[4096];
            int len;
            // is.read(buffer);
            // while ((len = is.read(buffer)) != -1) {
            //     System.out.println(len);
            //     sb.append(buffer);
            // }
            System.out.println(new String(buffer));
            // s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
