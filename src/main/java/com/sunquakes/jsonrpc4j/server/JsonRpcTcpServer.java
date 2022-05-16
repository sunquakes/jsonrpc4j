package com.sunquakes.jsonrpc4j.server;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonRpcTcpServer extends JsonRpcServer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        Thread t = new ServerThread(applicationContext);
        t.start();
    }
}

class ServerThread extends Thread {

    ExecutorService pool = Executors.newFixedThreadPool(10);

    private ApplicationContext applicationContext;

    ServerThread(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(3201);
            while (true) {
                Socket socket = server.accept();
                pool.execute(new JsonRpcTcpHandler(applicationContext, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
