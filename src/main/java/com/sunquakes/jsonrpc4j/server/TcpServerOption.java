package com.sunquakes.jsonrpc4j.server;

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
public class TcpServerOption {

    private String packageEof;

    private int packageMaxLength;

    private TcpServerPoolOption poolOption;
}
