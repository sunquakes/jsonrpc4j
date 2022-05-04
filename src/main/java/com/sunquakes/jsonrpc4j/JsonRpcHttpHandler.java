package com.sunquakes.jsonrpc4j;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class JsonRpcHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, 0);

        byte[] res = "test".getBytes();
        OutputStream out = httpExchange.getResponseBody();
        out.write(res);
        out.flush();
        out.close();
    }
}
