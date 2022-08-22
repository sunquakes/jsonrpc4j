package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.2
 * @since : 2022/8/21 4:12 PM
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jsonrpc.client")
public class JsonRpcClientConfig {

    private String packageEof = "\r\n";

    private int packageMaxLength = 4096;
}
