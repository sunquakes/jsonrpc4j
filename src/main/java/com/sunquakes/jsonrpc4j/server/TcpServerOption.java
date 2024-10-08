package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
public class TcpServerOption {

    private String packageEof;

    private int packageMaxLength;

    private TcpServerPoolOption poolOption;
}
