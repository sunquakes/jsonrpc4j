package com.sunquakes.jsonrpc4j;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class JsonRpcHttpHandler implements HttpHandler {

    private ApplicationContext applicationContext;

    JsonRpcHttpHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream is = httpExchange.getRequestBody();
        OutputStream out = httpExchange.getResponseBody();
        try {
            httpExchange.sendResponseHeaders(200, 0);
            Object clazz = applicationContext.getBean("JsonRpc");
            Method[] methods = clazz.getClass().getMethods();
            Method m = null;
            for (Method m2 : methods) {
                if (m2.getName().equals("add")) {
                    m = m2;
                }
            }
            Parameter[] ps = m.getParameters();
            if (ps != null) {
                for (int i = 0; i < ps.length; i++) {
                    System.out.println("java 8 ,paramter name = " + ps[i].getName());
                }
            }
            Object result = m.invoke(clazz, new Object[]{3, 4});

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("res", result);

            byte[] res = jsonObject.toString().getBytes();
            out.write(res);
        } catch (BeansException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }
}
