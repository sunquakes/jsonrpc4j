package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.NotifyRequestDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.InvalidRequestException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import com.sunquakes.jsonrpc4j.utils.ResponseUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.server
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@AllArgsConstructor
public class JsonRpcServerHandler {

    private ApplicationContext applicationContext;

    public Object handle(String json) {
        try {
            Object request = RequestUtils.parseRequestBody(json);
            if (request instanceof ArrayList) {
                return handleArray(request);
            } else if (request instanceof NotifyRequestDto || request instanceof RequestDto) {
                return handleObject(request);
            } else {
                throw new InvalidRequestException();
            }
        } catch (InvalidRequestException e) {
            return ResponseUtils.error(null, e.getCode(), e.getMessage());
        }
    }

    public Object handleObject(Object request) {
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
        try {
            String[] methodArr = RequestUtils.parseMethod(method);
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
            return ResponseUtils.success(id, result);
        } catch (InvocationTargetException | IllegalAccessException e) {
            return ResponseUtils.error(id, ErrorEnum.MethodNotFound.getCode());
        } catch (InvalidParamsException e) {
            return ResponseUtils.error(id, e.getCode(), e.getMessage());
        } catch (MethodNotFoundException e) {
            return ResponseUtils.error(id, e.getCode(), e.getMessage());
        }
    }

    public List<Object> handleArray(Object request) {
        List<Object> requestList = (List<Object>) request;
        return requestList.stream().map(item -> handleObject(item)).collect(Collectors.toList());
    }
}
