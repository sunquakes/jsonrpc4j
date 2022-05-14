package com.sunquakes.jsonrpc4j;

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
        if (!httpExchange.getRequestMethod().equals("POST")) {

        }
        httpExchange.sendResponseHeaders(200, 0);
        InputStream is = httpExchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        for (int ch; (ch = is.read()) != -1; ) {
            sb.append((char) ch);
        }
        String request = sb.toString();
        OutputStream out = httpExchange.getResponseBody();
        JsonRpcHandler jsonRpcHandler = new JsonRpcHandler(applicationContext);
        Object res = jsonRpcHandler.handle(request);
        System.out.println(res);
        byte[] a = JSON.toJSONBytes(res);
        out.write(a);
        out.flush();
        out.close();
    }
}
