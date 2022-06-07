package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.spring.JsonRpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring.boot
 * @Author: Robert
 * @CreateTime: 2022/5/30 12:45 PM
 **/
@SpringBootApplication
@JsonRpcScan({"com.sunquakes.jsonrpc4j.spring.boot"})
public class JsonRpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonRpcApplication.class, args);
    }
}
