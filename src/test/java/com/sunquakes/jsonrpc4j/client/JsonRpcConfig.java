package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.spring.JsonRpcScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@JsonRpcScan({"com.sunquakes"})
public class JsonRpcConfig {
}
