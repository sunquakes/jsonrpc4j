package com.sunquakes.jsonrpc4j.server;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/6/11 9:00 AM
 **/
@Data
@AllArgsConstructor
public class TcpServerPoolOption {

    private int maxActive;
}
