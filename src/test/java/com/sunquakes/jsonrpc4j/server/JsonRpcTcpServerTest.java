package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-tcp.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcTcpServerTest {

    @Value("${jsonrpc.server.package-eof}")
    private String packageEof;

    @Value("${jsonrpc.server.package-max-length}")
    private int packageMaxLength;

    @Test
    void testHandle() throws IOException {

        @Data
        @AllArgsConstructor
        class Params {
            int a;
            int b;
        }
        Params params = new Params(1, 2);
        RequestDto requestDto = new RequestDto("1234567890", "2.0", "JsonRpc/add", params);
        String request = JSONUtils.toString(requestDto);

        Socket s = new Socket("localhost", 3201);
        try {
            OutputStream os = s.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write((request + packageEof));
            bw.flush();
            os = s.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write((request + packageEof));
            bw.flush();
            byte[] buffer = new byte[packageMaxLength];
            int len;
            InputStream is = s.getInputStream();

            byte[] packageEofBytes = packageEof.getBytes();
            byte[] bytes = new byte[0];

            while ((len = is.read(buffer)) != -1) {
                int mergeLength = bytes.length + len;
                byte[] mergedArray = new byte[mergeLength];
                System.arraycopy(bytes, 0, mergedArray, 0, bytes.length);
                System.arraycopy(buffer, 0, mergedArray, bytes.length, mergeLength);
                bytes = mergedArray;

                int i = ByteArrayUtils.strstr(bytes, packageEofBytes, 0);
                if (i != -1) {
                    bytes = Arrays.copyOfRange(bytes, 0, i);
                    break;
                }
            }
            String sb = new String(bytes);
            ResponseDto responseDto = JSONUtils.parseJavaObject(sb, ResponseDto.class);
            assertEquals(3, responseDto.getResult());

            responseDto = JSONUtils.toJavaObject(ResponseDto.class, sb.toString());
            assertEquals(3, responseDto.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.close();
        }
    }
}
