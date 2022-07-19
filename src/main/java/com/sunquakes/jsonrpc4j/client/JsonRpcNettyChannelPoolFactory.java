package com.sunquakes.jsonrpc4j.client;

import io.netty.channel.Channel;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 7:24 PM
 **/
@UtilityClass
public class JsonRpcNettyChannelPoolFactory {

    ConcurrentHashMap poolMap = new ConcurrentHashMap();

    @Synchronized
    public GenericObjectPool getPool(String url, JsonRpcNettyChannelFactory jsonRpcNettyChannelFactory) {
        GenericObjectPool<Channel> objectPool = null;
        if (!poolMap.containsKey(url)) {
            GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();

            objectPool = new GenericObjectPool<>(jsonRpcNettyChannelFactory, genericObjectPoolConfig);
            // Maximum idle
            genericObjectPoolConfig.setMaxIdle(10);
            // Minimum idle
            genericObjectPoolConfig.setMinIdle(5);
            // Maximum connection
            genericObjectPoolConfig.setMaxTotal(100);

            genericObjectPoolConfig.setSoftMinEvictableIdleTime(Duration.ofSeconds(5));
            poolMap.putIfAbsent(url, objectPool);
        } else {
            objectPool = (GenericObjectPool<Channel>) poolMap.get(url);
        }
        return objectPool;
    }
}
