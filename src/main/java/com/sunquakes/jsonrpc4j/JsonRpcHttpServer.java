package com.sunquakes.jsonrpc4j;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class JsonRpcHttpServer extends JsonRpcServer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(3200), 0);
        httpServer.createContext("/", new JsonRpcHttpHandler(applicationContext));
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
    }
}
