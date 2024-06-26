package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
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
        JSONObject params = new JSONObject();
        params.put("a", 1);
        params.put("b", 2);
        JSONObject request = new JSONObject();
        request.put("id", "1234567890");
        request.put("jsonrpc", "2.0");
        request.put("method", "JsonRpc/add");
        request.put("params", params);

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
            StringBuffer sb = new StringBuffer();
            byte[] buffer = new byte[packageMaxLength];
            int bufferLength = buffer.length;
            int len;
            InputStream is = s.getInputStream();

            String init = "";
            int packageEofLength = packageEof.length();

            int i = sb.indexOf(packageEof);
            if (i != -1) {
                sb.substring(0, i);
                if (i + packageEofLength < sb.length()) {
                    init = sb.substring(i + packageEofLength);
                } else {
                    init = "";
                }
            } else {
                while ((len = is.read(buffer)) != -1) {
                    if (bufferLength == len) {
                        sb.append(new String(buffer));
                    } else {
                        byte[] end = Arrays.copyOfRange(buffer, 0, len);
                        sb.append(new String(end));
                    }
                    i = sb.indexOf(packageEof);
                    if (i != -1) {
                        sb.substring(0, i);
                        if (i + packageEofLength < sb.length()) {
                            init = sb.substring(i + packageEofLength);
                        } else {
                            init = "";
                        }
                        break;
                    }
                }
            }
            ResponseDto responseDto = JSONObject.parseObject(sb.toString(), ResponseDto.class);
            assertEquals(3, responseDto.getResult());

            sb = new StringBuffer(init);

            i = sb.indexOf(packageEof);
            if (i != -1) {
                sb.substring(0, i);
                if (i + packageEofLength < sb.length()) {
                    init = sb.substring(i + packageEofLength);
                } else {
                    init = "";
                }
            } else {
                while ((len = is.read(buffer)) != -1) {
                    if (bufferLength == len) {
                        sb.append(new String(buffer));
                    } else {
                        byte[] end = Arrays.copyOfRange(buffer, 0, len);
                        sb.append(new String(end));
                    }
                    i = sb.indexOf(packageEof);
                    if (i != -1) {
                        sb.substring(0, i);
                        if (i + packageEofLength < sb.length()) {
                            init = sb.substring(i + packageEofLength);
                        } else {
                            init = "";
                        }
                        break;
                    }
                }
            }

            responseDto = JSONObject.parseObject(sb.toString(), ResponseDto.class);
            assertEquals(3, responseDto.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.close();
        }
    }
}
