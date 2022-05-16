package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

@AllArgsConstructor
public class JsonRpcTcpHandler implements Runnable {

    private ApplicationContext applicationContext;

    private Socket socket;

    @Override
    public void run() {
        try {
            while (true) {
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[10];
                int bufferLength = buffer.length;
                int len;
                StringBuffer sb = new StringBuffer();

                String packageEof = "\r\n";
                int packageEofLength = packageEof.length();

                while ((len = is.read(buffer)) != -1) {
                    if (bufferLength == len) {
                        sb.append(new String(buffer));
                    } else {
                        byte[] end = Arrays.copyOfRange(buffer, 0, len);
                        sb.append(new String(end));
                    }
                    if (sb.substring(sb.length() - packageEofLength, sb.length() - 1).equals(packageEof)) {
                        break;
                    }
                }

                JsonRpcHandler jsonRpcHandler = new JsonRpcHandler(applicationContext);
                System.out.println(sb.substring(0, sb.length() - packageEofLength));
                Object res = jsonRpcHandler.handle(sb.substring(0, sb.length() - packageEofLength));
                System.out.println(res);
                byte[] output = JSON.toJSONBytes(res);

                OutputStream os = socket.getOutputStream();
                os.write(output);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
