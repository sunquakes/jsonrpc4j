package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.FixedChannelPool;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.1.0
 * @since 1.0.0
 **/
public class JsonRpcLoadBalancer {

    private final List<FixedChannelPool> pools = new CopyOnWriteArrayList<>();

    private final Bootstrap bootstrap;

    private final JsonRpcChannelPoolHandler poolHandler;

    private final Supplier<String> url;

    private final int defaultPort;

    private int times = 0;

    private final Object initLock = new Object();

    private static final int MAX_RETRY_TIMES = 3;

    public JsonRpcLoadBalancer(Supplier<String> url, int defaultPort, Bootstrap bootstrap, JsonRpcChannelPoolHandler poolHandler) {
        this.bootstrap = bootstrap;
        this.poolHandler = poolHandler;
        this.url = url;
        this.defaultPort = defaultPort;
    }

    public void initPools() {
        for (String item : url.get().split(",")) {
            if (!StringUtils.hasLength(item)) {
                continue;
            }
            String[] ipPort = item.split(":");
            String hostname = ipPort[0];
            int port = defaultPort;
            if (ipPort.length > 1) {
                port = Integer.parseInt(ipPort[1]);
            }
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            bootstrap.remoteAddress(address);
            pools.add(new FixedChannelPool(bootstrap, poolHandler, 10));
        }
    }

    public FixedChannelPool getPool() {
        if (pools.isEmpty()) {
            synchronized (initLock) {
                if (pools.isEmpty()) {
                    if (times >= MAX_RETRY_TIMES) {
                        times = 0;
                        throw new JsonRpcClientException("Fail to get service address.");
                    }
                    times++;
                    initPools();
                    if (!pools.isEmpty()) {
                        times = 0;
                    }
                }
            }
            if (pools.isEmpty()) {
                return getPool();
            }
        }
        return pools.get(ThreadLocalRandom.current().nextInt(pools.size()));
    }

    public boolean removePool(FixedChannelPool pool) {
        pool.close();
        return pools.remove(pool);
    }
}
