package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class JsonRpcHttpHandler implements HttpHandler {

    private ApplicationContext applicationContext;

    JsonRpcHttpHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream out = httpExchange.getResponseBody();
        httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        if (!httpExchange.getRequestMethod().equals("POST")) {
            httpExchange.sendResponseHeaders(405, 0);
            out.flush();
            out.close();
        }
        InputStream is = httpExchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        for (int ch; (ch = is.read()) != -1; ) {
            sb.append((char) ch);
        }
        String request = sb.toString();
        JsonRpcHandler jsonRpcHandler = new JsonRpcHandler(applicationContext);
        Object res = jsonRpcHandler.handle(request);
        byte[] output = JSON.toJSONBytes(res);
        httpExchange.sendResponseHeaders(200, output.length);
        out.write(output);
        out.flush();
        out.close();
    }
}
