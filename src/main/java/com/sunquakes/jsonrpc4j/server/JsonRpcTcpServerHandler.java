package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

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
            String init = "";
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            String packageEof = "\r\n";
            int packageEofLength = packageEof.length();
            while (!socket.isClosed()) {
                byte[] buffer = new byte[10];
                int bufferLength = buffer.length;
                int len;

                StringBuffer sb = new StringBuffer(init);
                // If more than one delimiter is received at a time
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
                if (sb.length() > 0) {
                    JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
                    Object res = jsonRpcServerHandler.handle(sb.substring(0, sb.length() - packageEofLength));
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
