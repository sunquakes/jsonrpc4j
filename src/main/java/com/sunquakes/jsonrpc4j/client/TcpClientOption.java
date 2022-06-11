package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.server.TcpServerPoolOption;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.server
 * @Author: Robert
 * @CreateTime: 2022/6/11 8:51 AM
 **/
@Data
@AllArgsConstructor
public class TcpClientOption {

    private String packageEof;

    private int packageMaxLength;
}
