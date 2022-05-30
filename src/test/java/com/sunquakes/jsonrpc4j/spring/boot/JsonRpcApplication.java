package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.spring.JsonRpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring.boot
 * @Author: Robert
 * @CreateTime: 2022/5/30 12:45 PM
 **/
@Configuration
@ComponentScan
@JsonRpcScan
public class JsonRpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonRpcApplication.class, args);
    }
}
