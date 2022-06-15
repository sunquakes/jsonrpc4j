package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/6/11 8:51 AM
 **/
@Data
@AllArgsConstructor
public class TcpServerOption {

    private String packageEof;

    private int packageMaxLength;

    private TcpServerPoolOption poolOption;
}
