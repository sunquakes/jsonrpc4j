package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.server
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@Slf4j
@AllArgsConstructor
public class JsonRpcTcpServerHandler implements Runnable {

    private ApplicationContext applicationContext;

    private Socket socket;

    @Override
    public void run() {
        try {
            byte[] initBytes = new byte[0];
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            String packageEof = "\r\n";
            byte[] packageEofBytes = packageEof.getBytes();
            int packageEofBytesLength = packageEofBytes.length;
            while (!socket.isClosed()) {
                byte[] buffer = new byte[10];
                int bufferLength = buffer.length;
                int len;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(initBytes);
                byte[] bytes = byteArrayOutputStream.toByteArray();

                // If more than one delimiter is received at a time
                int i = ByteArrayUtils.strstr(bytes, packageEofBytes);
                if (i != -1) {
                    if (i + packageEofBytesLength < bytes.length) {
                        initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
                    } else {
                        initBytes = new byte[0];
                    }
                    bytes = Arrays.copyOfRange(bytes, 0, i);
                } else {
                    while ((len = is.read(buffer)) != -1) {
                        if (bufferLength == len) {
                            byteArrayOutputStream.write(buffer);
                        } else {
                            byte[] end = Arrays.copyOfRange(buffer, 0, len);
                            byteArrayOutputStream.write(end);
                        }
                        bytes = byteArrayOutputStream.toByteArray();
                        i = ByteArrayUtils.strstr(bytes, packageEofBytes);
                        if (i != -1) {
                            if (i + packageEofBytesLength < bytes.length) {
                                initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
                            } else {
                                initBytes = new byte[0];
                            }
                            bytes = Arrays.copyOfRange(bytes, 0, i);
                            break;
                        }
                    }
                }
                if (bytes.length > 0) {
                    JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
                    System.out.println(new String(bytes));
                    Object res = jsonRpcServerHandler.handle(new String(bytes));
                    byte[] output = ByteArrayUtils.merge(JSON.toJSONBytes(res), packageEof.getBytes());
                    os.write(output);
                    os.flush();
                }
            }
        } catch (IOException e) {
            log.error("The socket is closed.");
        }
    }
}
