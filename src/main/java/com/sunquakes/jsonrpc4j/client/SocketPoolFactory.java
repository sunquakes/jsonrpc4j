package com.sunquakes.jsonrpc4j.client;

import lombok.experimental.UtilityClass;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 7:24 PM
 **/
@UtilityClass
public class SocketPoolFactory {

    ConcurrentHashMap poolMap = new ConcurrentHashMap();

    public GenericObjectPool getPool(String url) {
        GenericObjectPool<Socket> objectPool = null;
        if (!poolMap.contains(url)) {
            GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();

            objectPool = new GenericObjectPool<>(new JsonRpcTcpFactory(url), genericObjectPoolConfig);
            // Maximum idle
            genericObjectPoolConfig.setMaxIdle(10);
            // Minimum idle
            genericObjectPoolConfig.setMinIdle(5);
            // Maximum connection
            genericObjectPoolConfig.setMaxTotal(100);
            poolMap.put(url, objectPool);
        } else {
            poolMap.get(url);
        }
        return objectPool;
    }
}
