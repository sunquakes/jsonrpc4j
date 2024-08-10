package com.sunquakes.jsonrpc4j.utils;

import lombok.experimental.UtilityClass;

/**
 * @author : Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version : 2.1.1
 * @since : 2023/11/8 8:10 PM
 */
@UtilityClass
public class AddressUtils {

    public String getUrl(String hostname, Integer port) {
        return String.format("%s:%d", hostname, port);
    }
}
