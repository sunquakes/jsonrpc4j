package com.sunquakes.jsonrpc4j.server;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
public class JsonRpcHttpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", new JsonRpcHttpServerHandler(applicationContext));
        httpServer.setExecutor(Executors.newFixedThreadPool(poolMaxActive));
        httpServer.start();
    }
}
