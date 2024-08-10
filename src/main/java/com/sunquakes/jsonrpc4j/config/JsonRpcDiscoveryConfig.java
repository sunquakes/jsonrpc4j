package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.1.0
 * @since 1.0.0
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jsonrpc.discovery")
public class JsonRpcDiscoveryConfig {

    private List<String> url;

    private String driverName;
}
