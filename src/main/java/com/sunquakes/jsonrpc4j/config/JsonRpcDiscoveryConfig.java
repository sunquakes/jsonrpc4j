package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/5 11:51 AM
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jsonrpc.discovery")
public class JsonRpcDiscoveryConfig {

    private List<String> url;

    private String driverName;
}
