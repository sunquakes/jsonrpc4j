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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 **/
@AllArgsConstructor
public class JsonRpcServerHandler {

    private ApplicationContext applicationContext;

    private static final Map<String, Optional<Method>> METHOD_MAP = new ConcurrentHashMap<>();

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
        String method;
        String id;
        Object params;
        if (request instanceof NotifyRequestDto notifyRequestDto) {
            method = notifyRequestDto.getMethod();
            params = notifyRequestDto.getParams();
            id = null;
        } else {
            RequestDto requestDto = (RequestDto) request;
            method = requestDto.getMethod();
            params = requestDto.getParams();
            id = requestDto.getId();
        }
        try {
            String[] methodArr = RequestUtils.parseMethod(method);
            String className = methodArr[0];
            String methodName = methodArr[1];
            Object clazz = applicationContext.getBean(className);
            Method m;
            Optional<Method> cachedMethod = METHOD_MAP.get(method);
            if (cachedMethod != null) {
                m = cachedMethod.orElse(null);
            } else {
                m = null;
                for (Method candidate : clazz.getClass().getMethods()) {
                    if (candidate.getName().equals(methodName)) {
                        m = candidate;
                        break;
                    }
                }
                METHOD_MAP.put(method, Optional.ofNullable(m));
            }
            if (m == null) {
                throw new MethodNotFoundException();
            }
            Parameter[] paramsReflect = m.getParameters();
            Object[] paramArr = RequestUtils.parseParams(params, paramsReflect);
            Object result = m.invoke(clazz, paramArr);
            return ResponseUtils.success(id, result);
        } catch (InvocationTargetException | IllegalAccessException | BeansException e) {
            return ResponseUtils.error(id, ErrorEnum.METHOD_NOT_FOUND.getCode());
        } catch (InvalidParamsException e) {
            return ResponseUtils.error(id, ErrorEnum.INTERNAL_ERROR.getCode(), e.getMessage());
        } catch (MethodNotFoundException e) {
            return ResponseUtils.error(id, ErrorEnum.METHOD_NOT_FOUND.getCode(), e.getMessage());
        }
    }

    public List<Object> handleArray(Object request) {
        List<Object> requestList = (List<Object>) request;
        return requestList.stream().map(this::handleObject).toList();
    }
}
