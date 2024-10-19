package com.sunquakes.jsonrpc4j.utils;

import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.JSONException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Parameter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * @author : Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Slf4j
@UtilityClass
public class RequestUtils {

    public static final String TCP_PACKAGE_EOF = "\r\n";
    public static final int TCP_PACKAG_MAX_LENGHT = 2 * 1024 * 1024;

    public static final String JSONRPC = "2.0";

    public Object parseRequestBody(String json) throws InvalidRequestException {
        Object typeObject = JSONUtils.parse(json);
        if (JSONUtils.isArray(typeObject)) {
            return JSONUtils.toList(typeObject, (int index, Object item) -> parseSingleRequestBody(item));
        } else if (JSONUtils.isObject(typeObject)) {
            return parseSingleRequestBody(typeObject);
        } else {
            throw new InvalidRequestException();
        }
    }

    public Object parseSingleRequestBody(Object jsonObject) {
        if (JSONUtils.containsKey(jsonObject, "id")) {
            return JSONUtils.toJavaObject(RequestDto.class, jsonObject);
        } else {
            return JSONUtils.toJavaObject(NotifyRequestDto.class, jsonObject);
        }
    }

    public String[] parseMethod(String method) throws MethodNotFoundException {
        char first = method.charAt(0);
        if (first == '.' || first == '/') {
            method = method.substring(1);
        }
        int m = method.length() - method.replace("\\.", "").length();
        int n = method.length() - method.replace("/", "").length();
        if (m != 1 && n != 1) {
            throw new MethodNotFoundException(String.format("rpc: method request ill-formed: %s; need x.y or x/y", method));
        }
        String[] methodArr;
        if (m == 1) {
            methodArr = method.split("\\.");
        } else {
            methodArr = method.split("/");
        }
        return methodArr;
    }

    public Object[] parseParams(Object params, String[] names) throws InvalidParamsException {
        if (JSONUtils.isArray(params)) {
            return JSONUtils.toArray(params);
        } else if (JSONUtils.isObject(params)) {
            int l = names.length;
            Object[] res = new Object[l];
            for (int i = 0; i < l; i++) {
                res[i] = JSONUtils.get(params, names[i]);
            }
            return res;
        } else {
            throw new InvalidParamsException();
        }
    }

    public Object[] parseParams(Object params, Parameter[] paramsReflect) throws InvalidParamsException {
        int l = paramsReflect.length;
        if (JSONUtils.isArray(params)) {
            return JSONUtils.toArray(params, l, (int index, Object item) -> JSONUtils.toJavaObject(paramsReflect[index].getType(), item));
        } else if (JSONUtils.isObject(params)) {
            try {
                Object[] res = new Object[l];
                for (int i = 0; i < l; i++) {
                    String key = paramsReflect[i].getName();
                    if (!JSONUtils.containsKey(params, key)) {
                        throw new InvalidParamsException();
                    }
                    res[i] = JSONUtils.toJavaObject(paramsReflect[i].getType(), JSONUtils.get(params, key));
                }
                return res;
            } catch (JSONException e) {
                throw new InvalidParamsException();
            }
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
