package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonRpcClientChannelReleaseTest {

    @Test
    void httpClientReleasesChannelWhenSendFails() throws Exception {
        JsonRpcHttpClient client = mock(JsonRpcHttpClient.class, CALLS_REAL_METHODS);
        JsonRpcHttpClientHandler handler = mock(JsonRpcHttpClientHandler.class);
        ReflectionTestUtils.setField(client, "jsonRpcHttpClientHandler", handler);

        FixedChannelPool pool = mock(FixedChannelPool.class);
        Channel channel = acquiredChannel(client, pool);
        doThrow(new ExecutionException(new RuntimeException("send failed")))
                .when(handler).send(any(RequestDto.class), same(channel));

        assertThrows(JsonRpcClientException.class, () -> client.handle("JsonRpc/add", new Object[]{1, 2}));
        verify(pool).release(channel);
    }

    @Test
    void tcpClientReleasesChannelWhenSendFails() throws Exception {
        JsonRpcTcpClient client = mock(JsonRpcTcpClient.class, CALLS_REAL_METHODS);
        JsonRpcTcpClientHandler handler = mock(JsonRpcTcpClientHandler.class);
        ReflectionTestUtils.setField(client, "jsonRpcTcpClientHandler", handler);

        FixedChannelPool pool = mock(FixedChannelPool.class);
        Channel channel = acquiredChannel(client, pool);
        doThrow(new ExecutionException(new RuntimeException("send failed")))
                .when(handler).send(any(RequestDto.class), same(channel));

        assertThrows(JsonRpcClientException.class, () -> client.handle("JsonRpc/add", new Object[]{1, 2}));
        verify(pool).release(channel);
    }

    private Channel acquiredChannel(JsonRpcClient client, FixedChannelPool pool) throws Exception {
        JsonRpcLoadBalancer loadBalancer = mock(JsonRpcLoadBalancer.class);
        Channel channel = mock(Channel.class);
        @SuppressWarnings("unchecked")
        Future<Channel> acquireFuture = mock(Future.class);
        when(acquireFuture.get()).thenReturn(channel);
        when(pool.acquire()).thenReturn(acquireFuture);
        when(loadBalancer.getPool()).thenReturn(pool);
        client.loadBalancer = loadBalancer;
        return channel;
    }
}
