package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
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
