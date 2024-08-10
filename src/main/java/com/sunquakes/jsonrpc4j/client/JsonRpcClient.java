package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import com.sunquakes.jsonrpc4j.discovery.Driver;
import lombok.extern.slf4j.Slf4j;

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
}
