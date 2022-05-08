package com.sunquakes.jsonrpc4j.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RequestUtils {

    public String[] parseRequestMethod(String method) throws MethodNotFoundException {
        char first = method.charAt(0);
        if (first == '.' || first == '/') {
            method = method.substring(0, method.length() - 1);
        }
        if (method.indexOf(".") != 1 && method.indexOf("/") != 1) {
            throw new MethodNotFoundException(String.format("rpc: method request ill-formed: %s; need x.y or x/y", method));
        }
        String[] methodArr;
        if (method.indexOf(".") == 1) {
            methodArr = method.split(".");
        } else {
            methodArr = method.split("/");
        }
        return methodArr;
    }

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
}
