package com.sunquakes.jsonrpc4j.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.exception.JSONException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class JSONUtils {

    public String toString(Object object) {
        return JSON.toJSONString(object);
    }

    public <T> T toJavaObject(Class<T> clazz, Object object) {
        return JSON.to(clazz, object);
    }

    public byte[] toBytes(Object object) {
        return JSON.toJSONBytes(object);
    }

    public <T> T parseJavaObject(String object, Class<T> clazz) {
        return JSONObject.parseObject(object, clazz);
    }

    public Object parseJSONObject(String object) {
        return JSONObject.parseObject(object);
    }

    public <T> List<T> parseList(String object, Class<T> clazz) {
        return JSONArray.parseArray(object, clazz);
    }

    public Object parse(String object) {
        return JSON.parse(object);
    }

    public boolean isArray(Object object) {
        return object instanceof JSONArray;
    }

    public boolean isObject(Object object) {
        return object instanceof JSONObject;
    }

    public List<Object> toList(Object object, Callback callback) {
        JSONArray jsonArray = (JSONArray) object;
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            list.add(callback.call(i, jsonObject));
        }
        return list;
    }

    public boolean containsKey(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        return jsonObject.containsKey(key);
    }

    public Object[] toArray(Object object, int length, Callback callback) {
        JSONArray jsonArray = (JSONArray) object;
        Object[] arr = new Object[length];
        for (int i = 0; i < length; i++) {
            Object item = jsonArray.get(i);
            arr[i] = callback.call(i, item);
        }
        return arr;
    }

    public Object get(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        return jsonObject.get(key);
    }

    public Object[] toArray(Object object) {
        JSONArray jsonArray = (JSONArray) object;
        return jsonArray.toArray();
    }

    interface Callback {
        Object call(int index, Object object) throws JSONException;
    }
}
