package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 7:24 PM
 **/
@UtilityClass
public class JsonRpcChannelPoolFactory {

    private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;

     ConcurrentHashMap<String, ConcurrentHashMap<String, FixedChannelPool>> poolMapMap = new ConcurrentHashMap();

    @Synchronized
    public FixedChannelPool getPool(String name, String url, Bootstrap bootstrap, JsonRpcChannelPoolHandler handler, int defaultPort) {
        String[] urls = url.split(",");
        FixedChannelPool channelPool;
        Random random = new Random();
        ConcurrentHashMap<String, FixedChannelPool> poolMap = new ConcurrentHashMap<>();
        if (!poolMapMap.containsKey(name)) {
            poolMapMap.putIfAbsent(name, poolMap);
        }
        poolMap = poolMapMap.get(name);
        int index = random.nextInt(urls.length);
        String key = urls[index];
        if (!poolMap.contains(key)) {
            String[] ipPort = key.split(":");
            String hostname = ipPort[0];
            int port = defaultPort;
            if (ipPort.length > 1) {
                port = Integer.parseInt(ipPort[1]);
            }
            bootstrap.remoteAddress(new InetSocketAddress(hostname, port));
            channelPool = new FixedChannelPool(bootstrap, handler, 10);
            poolMap.putIfAbsent(key, channelPool);
        } else {
            channelPool = poolMap.get(key);
        }
        return channelPool;
    }
}
