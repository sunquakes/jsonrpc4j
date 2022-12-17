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
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Parameter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Slf4j
@UtilityClass
public class RequestUtils {

    public String PROTOCOL_TCP = "tcp";
    public String PROTOCOL_HTTP = "http";
    public String PROTOCOL_HTTPS = "https";

    public String TCP_PACKAGE_EOF = "\r\n";
    public int TCP_PACKAG_MAX_LENGHT = 2 * 1024 * 1024;

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

    @Deprecated
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

    public Object[] parseParams(Object params, Parameter[] paramsReflect) throws InvalidParamsException {
        int l = paramsReflect.length;
        if (params instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) params;
            Object[] res = new Object[l];
            for (int i = 0; i < l; i++) {
                Object item = jsonArray.get(i);
                res[i] = JSON.toJavaObject(item, paramsReflect[i].getType());
            }
            return res;
        } else if (params instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) params;
            Object[] res = new Object[l];
            for (int i = 0; i < l; i++) {
                String key = paramsReflect[i].getName();
                if (!jsonObject.containsKey(key)) {
                    throw new InvalidParamsException();
                }
                res[i] = JSON.toJavaObject(jsonObject.get(key), paramsReflect[i].getType());
            }
            return res;
        } else {
            throw new InvalidParamsException();
        }
    }

    public String getId() {
        return UUID.randomUUID().toString();
    }

    public String getLocalIp() {
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        return "127.0.0.1";
    }
}
