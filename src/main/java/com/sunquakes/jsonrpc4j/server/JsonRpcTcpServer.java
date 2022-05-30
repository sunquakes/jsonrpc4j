package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.client
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
public class JsonRpcTcpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        Thread t = new ServerThread(applicationContext, port, poolMaxActive);
        t.start();
    }
}

class ServerThread extends Thread {

    private ApplicationContext applicationContext;

    private int port;

    ExecutorService pool;

    ServerThread(ApplicationContext applicationContext, int port, int poolMaxActive) {
        this.applicationContext = applicationContext;
        this.port = port;
        this.pool = Executors.newFixedThreadPool(poolMaxActive);
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                pool.execute(new JsonRpcTcpServerHandler(applicationContext, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
