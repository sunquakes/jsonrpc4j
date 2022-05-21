package com.sunquakes.jsonrpc4j.server;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.server
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
public class JsonRpcHttpServer extends JsonRpcServer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(3200), 0);
        httpServer.createContext("/", new JsonRpcHttpServerHandler(applicationContext));
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
    }
}
