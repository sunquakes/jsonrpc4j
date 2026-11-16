package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class JsonRpcLoadBalancerTest {

    @Test
    void testGetPoolInitializesLazily() throws ExecutionException, InterruptedException {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        AtomicInteger urlCalls = new AtomicInteger();
        try (MockedConstruction<FixedChannelPool> mocked = mockConstruction(FixedChannelPool.class,
                (mock, context) -> {
                    Channel channel = mock(Channel.class);
                    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 80));
                    @SuppressWarnings("unchecked")
                    Future<Channel> future = mock(Future.class);
                    when(future.get()).thenReturn(channel);
                    when(mock.acquire()).thenReturn(future);
                })) {
            JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(
                    () -> {
                        urlCalls.incrementAndGet();
                        return "localhost";
                    }, 80, bootstrap, handler);

            assertEquals(new InetSocketAddress("localhost", 80),
                    loadBalancer.getPool().acquire().get().remoteAddress());
            assertEquals(1, urlCalls.get());
            assertEquals(1, mocked.constructed().size());
        }
    }

    @Test
    void testInitPoolsSkipsEmptyAddresses() {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        try (MockedConstruction<FixedChannelPool> mocked = mockConstruction(FixedChannelPool.class)) {
            JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(
                    () -> "localhost:8001,,localhost:8002,localhost:8003", 80, bootstrap, handler);

            loadBalancer.initPools();

            assertEquals(3, mocked.constructed().size());
        }
    }

    @Test
    void testRetryStopsAfterThreeAttempts() {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        AtomicInteger urlCalls = new AtomicInteger();
        JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(
                () -> {
                    urlCalls.incrementAndGet();
                    return "";
                }, 80, bootstrap, handler);

        assertThrows(JsonRpcClientException.class, loadBalancer::getPool);
        assertEquals(3, urlCalls.get());
    }

    @Test
    void testRetryRecoversWhenAddressBecomesAvailable() {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        AtomicInteger urlCalls = new AtomicInteger();
        try (MockedConstruction<FixedChannelPool> mocked = mockConstruction(FixedChannelPool.class)) {
            JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(
                    () -> urlCalls.incrementAndGet() < 3 ? "" : "localhost", 80, bootstrap, handler);

            loadBalancer.getPool();

            assertEquals(3, urlCalls.get());
            assertEquals(1, mocked.constructed().size());
        }
    }
}
