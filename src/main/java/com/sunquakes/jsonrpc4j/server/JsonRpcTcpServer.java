package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonRpcTcpServer extends JsonRpcServer implements InitializingBean {

    ExecutorService pool = Executors.newFixedThreadPool(10);

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        ServerSocket server = new ServerSocket(3201);
        while (true) {
            Socket conn = server.accept();
            pool.execute(new ServerThread(conn));
        }
    }
}

@AllArgsConstructor
class ServerThread implements Runnable {

    private Socket socket;

    @Override
    public void run() {
        try {
            while (true) {
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = is.read(buffer)) != -1) {
                    sb.append(buffer);
                }
                OutputStream os = socket.getOutputStream();
                os.write("ab".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
