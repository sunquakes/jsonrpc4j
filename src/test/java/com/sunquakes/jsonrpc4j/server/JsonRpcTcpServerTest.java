package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
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
import java.util.Arrays;

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
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write((request + "\r\n"));
            bw.flush();
            System.out.println(666);
            os = s.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write((request + "\r\n"));
            bw.flush();
            StringBuffer sb = new StringBuffer();
            byte[] buffer = new byte[4096];
            int bufferLength = buffer.length;
            int len;
            InputStream is = s.getInputStream();

            String init = "";
            String packageEof = "\r\n";
            int packageEofLength = packageEof.length();

            while ((len = is.read(buffer)) != -1) {
                if (bufferLength == len) {
                    sb.append(new String(buffer));
                } else {
                    byte[] end = Arrays.copyOfRange(buffer, 0, len);
                    sb.append(new String(end));
                }
                int i = sb.indexOf(packageEof);
                if (i != -1) {
                    sb.substring(0, i);
                    if (i + packageEofLength < sb.length()) {
                        init = sb.substring(i + packageEofLength + 1);
                    }
                    break;
                }
            }
            ResponseDto responseDto = JSONObject.parseObject(sb.toString(), ResponseDto.class);
            assertEquals(responseDto.getResult(), 3);
            System.out.println(sb);
            // is.read(buffer);
            // while ((len = is.read(buffer)) != -1) {
            //     System.out.println(len);
            //     sb.append(buffer);
            // }
            // s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
