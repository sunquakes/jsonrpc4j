package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

@AllArgsConstructor
public class JsonRpcTcpHandler implements Runnable {

    private ApplicationContext applicationContext;

    private Socket socket;

    @Override
    public void run() {
        try {
            String init = "";
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            while (!socket.isClosed()) {
                byte[] buffer = new byte[10];
                int bufferLength = buffer.length;
                int len;

                StringBuffer sb = new StringBuffer(init);
                String packageEof = "\r\n";
                int packageEofLength = packageEof.length();

                while ((len = is.read(buffer)) != -1) {
                    System.out.println(len);
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
                            init = sb.substring(i + packageEofLength);
                        } else {
                            init = "";
                        }
                        break;
                    }
                }
                if (sb.length() > 0) {
                    JsonRpcHandler jsonRpcHandler = new JsonRpcHandler(applicationContext);
                    System.out.println("sb");
                    System.out.println(sb);
                    Object res = jsonRpcHandler.handle(sb.substring(0, sb.length() - packageEofLength));
                    System.out.println(res);
                    byte[] output = ByteArrayUtils.merge(JSON.toJSONBytes(res), packageEof.getBytes());
                    os.write(output);
                    os.flush();
                }
            }
        } catch (IOException e) {
            System.out.println(888);
            // e.printStackTrace();
        }
    }
}
