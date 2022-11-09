package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class JsonRpcLoadBalancerTest {

    private AutoCloseable closeable;

    @Before
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetPool() throws ExecutionException, InterruptedException {
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
    public void testRetry() {
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
