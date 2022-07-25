package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 7:24 PM
 **/
@UtilityClass
public class JsonRpcNettyChannelPoolFactory {

    private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;

    ConcurrentHashMap poolMap = new ConcurrentHashMap();

    @Synchronized
    public FixedChannelPool getPool(String url, Bootstrap bootstrap, JsonRpcNettyChannelPoolHandler handler) {
        FixedChannelPool channelPool;
        if (!poolMap.containsKey(url)) {
            channelPool = new FixedChannelPool(bootstrap, handler, 10);
            poolMap.putIfAbsent(url, channelPool);
        } else {
            channelPool = (FixedChannelPool) poolMap.get(url);
        }
        return channelPool;
    }
}
