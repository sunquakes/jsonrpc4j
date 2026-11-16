package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import com.sunquakes.jsonrpc4j.discovery.Driver;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.1.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcClient {

    protected Config<Object> config;

    protected String protocol;

    protected String name;

    protected ConfigEntry<String> url;

    protected ConfigEntry<Driver> discovery;

    private static final String DISCOVERY_KEY = "discovery";

    JsonRpcLoadBalancer loadBalancer;

    JsonRpcClient(Config<Object> config) {
        this.config = config;
        if (config.get("url") != null) {
            url = new ConfigEntry<>("url", (String) config.get("url").value());
        }
        if (config.get(DISCOVERY_KEY) != null) {
            discovery = new ConfigEntry<>(DISCOVERY_KEY, (Driver) config.get(DISCOVERY_KEY).value());
        }
        protocol = (String) config.get("protocol").value();
        name = (String) config.get("name").value();
        initLoadBalancer();
    }

    protected void initLoadBalancer() {
        // Call child class method.
    }

    protected Object handle(String method, Object[] args, JsonRpcClientHandler clientHandler) throws JsonRpcException {
        RequestDto requestDto = new RequestDto(RequestUtils.getId(), RequestUtils.JSONRPC, method, args);
        ResponseDto responseDto;
        String body;
        FixedChannelPool pool = loadBalancer.getPool();
        Channel channel = null;
        try {
            channel = pool.acquire().get();
            body = clientHandler.send(requestDto, channel);
            pool.release(channel);
            channel = null;
            responseDto = JSONUtils.parseJavaObject(body, ResponseDto.class);
        } catch (InterruptedException e) {
            loadBalancer.removePool(pool);
            Thread.currentThread().interrupt();
            throw new JsonRpcClientException(e.getMessage());
        } catch (ExecutionException e) {
            throw new JsonRpcClientException(e.getMessage());
        } finally {
            if (channel != null) {
                pool.release(channel);
            }
        }
        if (responseDto.getResult() == null) {
            Object error = JSONUtils.get(JSONUtils.parseJSONObject(body), "error");
            if (error != null) {
                ErrorDto errorDto = JSONUtils.parseJavaObject(JSONUtils.toString(error), ErrorDto.class);
                throw ErrorEnum.getException(errorDto.getCode(), errorDto.getMessage());
            }
        }
        return responseDto.getResult();
    }
}
