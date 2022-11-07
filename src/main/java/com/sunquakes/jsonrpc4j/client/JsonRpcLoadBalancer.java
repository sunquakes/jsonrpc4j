package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.FixedChannelPool;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/7 0:23 PM
 **/
public class JsonRpcLoadBalancer {

    private final List<FixedChannelPool> pools = new CopyOnWriteArrayList<>();

    private final Bootstrap bootstrap;

    private final JsonRpcChannelPoolHandler poolHandler;

    private final Supplier<String> url;

    private final int defaultPort;

    public JsonRpcLoadBalancer(Supplier<String> url, int defaultPort, Bootstrap bootstrap, JsonRpcChannelPoolHandler poolHandler) {
        this.bootstrap = bootstrap;
        this.poolHandler = poolHandler;
        this.url = url;
        this.defaultPort = defaultPort;
    }

    public void initPools() {
        Arrays.asList(url.get().split(",")).stream().forEach(item -> {
            String[] ipPort = item.split(":");
            String hostname = ipPort[0];
            int port = defaultPort;
            if (ipPort.length > 1) {
                port = Integer.parseInt(ipPort[1]);
            }
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            bootstrap.remoteAddress(address);
            FixedChannelPool pool = new FixedChannelPool(bootstrap, poolHandler, 10);
            pools.add(pool);
        });
    }

    public FixedChannelPool getPool() {
        FixedChannelPool channelPool;
        if (pools.size() <= 0) {
            initPools();
            return getPool();
        } else {
            int index = ThreadLocalRandom.current().nextInt(pools.size());
            channelPool = pools.get(index);
        }
        return channelPool;
    }
}
