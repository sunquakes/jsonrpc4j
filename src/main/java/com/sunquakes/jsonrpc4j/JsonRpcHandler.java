package com.sunquakes.jsonrpc4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsonRpcHandler implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public byte[] handle(String json) {
        try {
            Object typeObject = JSON.parse(json);
            if (typeObject instanceof JSONArray) {
                System.out.print("JSONArray");
            } else if (typeObject instanceof JSONObject) {
                System.out.print("JSONObject");
            } else {

            }
        } catch (JSONException e) {

        }
        return null;
    }

    public JSONObject handlerObject(JSONObject jsonObject) {
        try {
            Object clazz = applicationContext.getBean("JsonRpc");
            Method m = clazz.getClass().getDeclaredMethod("add");
            Object result = m.invoke(clazz);

            byte[] res = jsonObject.toString().getBytes();
        } catch (BeansException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
        }
        return jsonObject;
    }

    public JSONArray handlerArray(JSONArray jsonArray) {
        return jsonArray;
    }
}
