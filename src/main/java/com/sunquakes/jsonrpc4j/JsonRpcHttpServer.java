package com.sunquakes.jsonrpc4j;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class JsonRpcHttpServer extends JsonRpcServer {

    public void start() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(3200), 0);
        httpServer.createContext("/", new JsonRpcHttpHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
    }
}
