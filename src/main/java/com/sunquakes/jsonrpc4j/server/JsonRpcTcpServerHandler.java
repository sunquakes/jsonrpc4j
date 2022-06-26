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
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Slf4j
@AllArgsConstructor
public class JsonRpcTcpServerHandler implements Runnable {

    private ApplicationContext applicationContext;

    private Socket socket;

    private TcpServerOption tcpServerOption;

    @Override
    public void run() {
        try {
            byte[] initBytes = new byte[0];
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            String packageEof = tcpServerOption.getPackageEof();
            int bufferSize = tcpServerOption.getPackageMaxLength();
            byte[] packageEofBytes = packageEof.getBytes();
            int packageEofBytesLength = packageEofBytes.length;
            byte[] buffer = new byte[bufferSize];
            while (!socket.isClosed()) {
                int len;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(initBytes);
                byte[] bytes = byteArrayOutputStream.toByteArray();

                // If the length of the data is smaller than package EOF's length, read stream to the OutputStream only.
                if (bytes.length < packageEofBytesLength) {
                    int endIndex = 0;
                    while ((len = is.read(buffer)) != -1) {
                        if (bufferSize == len) {
                            byteArrayOutputStream.write(buffer);
                        } else {
                            byte[] end = Arrays.copyOfRange(buffer, 0, len);
                            byteArrayOutputStream.write(end);
                        }
                        bytes = byteArrayOutputStream.toByteArray();

                        if (bytes.length > packageEofBytesLength) {
                            if (endIndex == 0) {
                                endIndex = packageEofBytesLength;
                            }
                            int i = ByteArrayUtils.strstr(bytes, packageEofBytes, endIndex - packageEofBytesLength);
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
                        endIndex += len;
                    }
                } else {
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
                        int endIndex = bytes.length;
                        while ((len = is.read(buffer)) != -1) {
                            if (bufferSize == len) {
                                byteArrayOutputStream.write(buffer);
                            } else {
                                byte[] end = Arrays.copyOfRange(buffer, 0, len);
                                byteArrayOutputStream.write(end);
                            }
                            bytes = byteArrayOutputStream.toByteArray();
                            i = ByteArrayUtils.strstr(bytes, packageEofBytes, endIndex - packageEofBytesLength);
                            if (i != -1) {
                                if (i + packageEofBytesLength < bytes.length) {
                                    initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
                                } else {
                                    initBytes = new byte[0];
                                }
                                bytes = Arrays.copyOfRange(bytes, 0, i);
                                break;
                            }
                            endIndex += len;
                        }
                    }
                }
                if (bytes.length > 0) {
                    JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
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
