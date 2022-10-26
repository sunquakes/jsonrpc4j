package com.sunquakes.jsonrpc4j.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/10/26 0:32 PM
 **/
@UtilityClass
public class RobinUtils {

    public String getServer(String server) {
        String[] servers = server.split(",");
        Random random = new Random();
        int index = random.nextInt(servers.length);
        return servers[index];
    }
}
