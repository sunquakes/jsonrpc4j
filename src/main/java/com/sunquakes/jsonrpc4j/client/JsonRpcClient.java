package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import com.sunquakes.jsonrpc4j.discovery.Driver;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/5 10:26 PM
 **/
@Slf4j
public class JsonRpcClient {

    protected Config config;

    protected String protocol;

    protected String name;

    protected ConfigEntry<String> url;

    protected ConfigEntry<Driver> discovery;

    JsonRpcLoadBalancer loadBalancer;

    JsonRpcClient(Config config) {
        this.config = config;
        url = config.get("url");
        discovery = config.get("discovery");
        protocol = (String) config.get("protocol").value();
        name = (String) config.get("name").value();
        initLoadBalancer();
    }

    protected void initLoadBalancer() {
    }
}
