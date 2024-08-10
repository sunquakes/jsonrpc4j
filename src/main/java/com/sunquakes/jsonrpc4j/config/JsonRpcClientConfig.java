package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.2
 * @since 1.0.0
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jsonrpc.client")
public class JsonRpcClientConfig {

    private String packageEof = "\\r\\n";

    private int packageMaxLength = 4096;
}
