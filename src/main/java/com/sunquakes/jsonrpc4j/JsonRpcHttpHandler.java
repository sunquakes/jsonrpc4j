package com.sunquakes.jsonrpc4j;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsonRpcHttpHandler implements HttpHandler {

    private ApplicationContext applicationContext;

    JsonRpcHttpHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            httpExchange.sendResponseHeaders(200, 0);
            Object clazz = applicationContext.getBean("JsonRpc");
            Method m = clazz.getClass().getDeclaredMethod("add");
            Object result = m.invoke(clazz);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("res", result);

            byte[] res = jsonObject.toString().getBytes();
            OutputStream out = httpExchange.getResponseBody();
            out.write(res);
            out.flush();
            out.close();
        } catch (BeansException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
