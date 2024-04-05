package com.sunquakes.jsonrpc4j.client;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Shing Rui <sunquakes@outlook.com>
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
public class TcpClientOption {

    private String packageEof;

    private int packageMaxLength;
}
