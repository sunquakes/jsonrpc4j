package com.sunquakes.jsonrpc4j.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.utils
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@UtilityClass
public class RequestUtils {

    public String JSONRPC = "2.0";

    public Object parseRequestBody(String json) throws InvalidRequestException {
        Object typeObject = JSON.parse(json);
        if (typeObject instanceof JSONArray) {
            List<Object> list = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) typeObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                list.add(parseSingleRequestBody(jsonObject));
            }
            return list;
        } else if (typeObject instanceof JSONObject) {
            return parseSingleRequestBody((JSONObject) typeObject);
        } else {
            throw new InvalidRequestException();
        }
    }

    public Object parseSingleRequestBody(JSONObject jsonObject) {
        if (jsonObject.containsKey("id")) {
            return jsonObject.toJavaObject(RequestDto.class);
        } else {
            return jsonObject.toJavaObject(NotifyRequestDto.class);
        }
    }

    public String[] parseMethod(String method) throws MethodNotFoundException {
        char first = method.charAt(0);
        if (first == '.' || first == '/') {
            method = method.substring(1);
        }
        int m = method.length() - method.replaceAll("\\.", "").length();
        int n = method.length() - method.replaceAll("/", "").length();
        if (m != 1 && n != 1) {
            throw new MethodNotFoundException(String.format("rpc: method request ill-formed: %s; need x.y or x/y", method));
        }
        String[] methodArr;
        if (m == 1) {
            methodArr = method.split(".");
        } else {
            methodArr = method.split("/");
        }
        return methodArr;
    }

    public Object[] parseParams(Object params, String[] names) throws InvalidParamsException {
        if (params instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) params;
            return jsonArray.toArray();
        } else if (params instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) params;
            int l = names.length;
            Object[] res = new Object[l];
            for (int i = 0; i < l; i++) {
                res[i] = jsonObject.get(names[i]);
            }
            return res;
        } else {
            throw new InvalidParamsException();
        }
    }

    public String getId() {
        return UUID.randomUUID().toString();
    }
}
