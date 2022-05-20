package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.spring.JsonRpcClientImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JsonRpcClientImportBeanDefinitionRegistrar.class)
public class JsonRpcConfig {
}
