package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class JsonRpcLoadBalancerTest {

    private AutoCloseable closeable;

    @BeforeEach
    void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testGetPool() throws ExecutionException, InterruptedException {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        String url = "localhost";
        try (MockedConstruction<FixedChannelPool> mocked = mockConstruction(FixedChannelPool.class,
                (mock, context) -> {
                    Channel channel = mock(Channel.class);
                    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 80));
                    Future feature =  mock(Future.class);
                    when(feature.get()).thenReturn(channel);
                    when(mock.acquire()).thenReturn(feature);
                })) {

            JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(() -> url, 80, bootstrap, handler);
            loadBalancer.initPools();
            assertEquals(loadBalancer.getPool().acquire().get().remoteAddress(), new InetSocketAddress("localhost", 80));
        }
    }

    @Test
    void testRetry() {
        Bootstrap bootstrap = mock(Bootstrap.class);
        JsonRpcChannelPoolHandler handler = mock(JsonRpcChannelPoolHandler.class);
        String url = "";
        try (MockedConstruction<FixedChannelPool> mocked = mockConstruction(FixedChannelPool.class,
                (mock, context) -> {
                    Channel channel = mock(Channel.class);
                    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 80));
                    Future feature =  mock(Future.class);
                    when(feature.get()).thenReturn(channel);
                    when(mock.acquire()).thenReturn(feature);
                })) {

            JsonRpcLoadBalancer loadBalancer = new JsonRpcLoadBalancer(() -> url, 80, bootstrap, handler);
            loadBalancer.initPools();
            assertThrows(JsonRpcClientException.class, () -> {
                loadBalancer.getPool();
            });
        }
    }
}
