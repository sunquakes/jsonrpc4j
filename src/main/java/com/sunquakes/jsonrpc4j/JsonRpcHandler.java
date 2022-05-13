package com.sunquakes.jsonrpc4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import com.sunquakes.jsonrpc4j.utils.ResponseUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonRpcHandler implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object handle(String json) {
        try {
            Object request = RequestUtils.parseRequestBody(json);
            if (request instanceof ArrayList) {
                return handleArray(request);
            } else if (request instanceof NotifyRequestDto || request instanceof RequestDto) {
                return handleObject(request);
            } else {

            }
        } catch (JSONException e) {

        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (MethodNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object handleObject(Object request) throws MethodNotFoundException {
        String method, id = null;
        Object params;
        if (request instanceof NotifyRequestDto) {
            NotifyRequestDto notifyRequestDto = (NotifyRequestDto) request;
            method = notifyRequestDto.getMethod();
            params = notifyRequestDto.getParams();
        } else {
            RequestDto requestDto = (RequestDto) request;
            method = requestDto.getMethod();
            params = requestDto.getParams();
            id = requestDto.getId();
        }
        String[] methodArr = RequestUtils.parseMethod(method);
        try {
            Object clazz = applicationContext.getBean(methodArr[0]);
            Method[] methods = clazz.getClass().getMethods();
            Method m = null;
            for (Method m2 : methods) {
                if (m2.getName().equals(methodArr[1])) {
                    m = m2;
                    break;
                }
            }
            Parameter[] paramsReflect = m.getParameters();
            String[] paramNames = new String[paramsReflect.length];
            if (params != null) {
                for (int i = 0; i < paramsReflect.length; i++) {
                    paramNames[i] = paramsReflect[i].getName();
                }
            }
            Object[] paramArr = RequestUtils.parseParams(params, paramNames);
            Object result = m.invoke(clazz, paramArr);
            if (request instanceof NotifyRequestDto) {
                return ResponseUtils.successNotify(result);
            } else {
                return ResponseUtils.success(id, result);
            }
        } catch (BeansException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvalidParamsException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object> handleArray(Object request) {
        List<Object> response = new ArrayList<>();
        List<Object> requestList = (List<Object>) request;
        requestList.stream().map(item -> {
            Object res = null;
            try {
                res = handleObject(item);
            } catch (MethodNotFoundException e) {
            }
            return res;
        }).collect(Collectors.toList());
        return response;
    }
}
