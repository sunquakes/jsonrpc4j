package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.server
 * @Author: Robert
 * @CreateTime: 2022/6/11 9:00 AM
 **/
@Data
@AllArgsConstructor
public class TcpServerPoolOption {

    private int maxActive;
}
