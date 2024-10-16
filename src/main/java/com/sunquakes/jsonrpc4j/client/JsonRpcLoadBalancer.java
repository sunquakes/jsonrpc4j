package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.FixedChannelPool;
import lombok.Synchronized;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private static final int MAX_RETRY_TIMES = 3;

    private static final SecureRandom secureRandom = new SecureRandom();

    public JsonRpcLoadBalancer(Supplier<String> url, int defaultPort, Bootstrap bootstrap, JsonRpcChannelPoolHandler poolHandler) {
        this.bootstrap = bootstrap;
        this.poolHandler = poolHandler;
        this.url = url;
        this.defaultPort = defaultPort;
    }

    public void initPools() {
        Arrays.asList(url.get().split(",")).stream().filter(StringUtils::hasLength).forEach(item -> {
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

    @Synchronized
    public FixedChannelPool getPool() {
        FixedChannelPool pool;
        if (pools.isEmpty()) {
            if (times >= MAX_RETRY_TIMES) {
                times = 0;
                throw new JsonRpcClientException("Fail to get service address.");
            }
            times++;
            initPools();
            return getPool();
        } else {
            secureRandom.nextBytes(new byte[8]);
            int index = secureRandom.nextInt(pools.size());
            pool = pools.get(index);
        }
        return pool;
    }

    public boolean removePool(FixedChannelPool pool) {
        pool.close();
        return pools.remove(pool);
    }
}
