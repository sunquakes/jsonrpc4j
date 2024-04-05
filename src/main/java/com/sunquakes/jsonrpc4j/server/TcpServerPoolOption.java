package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Shing Rui <sunquakes@outlook.com>
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
public class TcpServerPoolOption {

    private int maxActive;
}
