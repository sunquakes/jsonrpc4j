package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 7:24 PM
 **/
@UtilityClass
public class JsonRpcNettyChannelPoolFactory {

    private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;

    ConcurrentHashMap<InetSocketAddress, FixedChannelPool> poolMap = new ConcurrentHashMap();

    @Synchronized
    public FixedChannelPool getPool(InetSocketAddress address, Bootstrap bootstrap, JsonRpcNettyChannelPoolHandler handler) {
        FixedChannelPool channelPool;
        if (!poolMap.containsKey(address)) {
            channelPool = new FixedChannelPool(bootstrap, handler, 10);
            poolMap.putIfAbsent(address, channelPool);
        } else {
            channelPool = (FixedChannelPool) poolMap.get(address);
        }
        return channelPool;
    }
}
