package com.sunquakes.jsonrpc4j.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonRpcTcpClientHandlerTest {

    private JsonRpcTcpClientHandler handler;
    private Channel channel;
    private ChannelHandlerContext context;

    @BeforeEach
    void setUp() {
        channel = mock(Channel.class);
        context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(channel);
    }

    @Test
    void resolvesFragmentedUtf8BodyAndMultibyteDelimiter() throws Exception {
        String eof = "结束";
        handler = new JsonRpcTcpClientHandler(new TcpClientOption(eof, 1024));
        String body = "{\"name\":\"小明\"}";
        byte[] frame = (body + eof).getBytes(StandardCharsets.UTF_8);
        int bodyLength = body.getBytes(StandardCharsets.UTF_8).length;
        Promise<String> promise = newPromise();

        handler.channelRead(context, Arrays.copyOfRange(frame, 0, bodyLength - 1));
        assertNull(promise.getNow());
        handler.channelRead(context, Arrays.copyOfRange(frame, bodyLength - 1, bodyLength + 2));
        assertNull(promise.getNow());
        handler.channelRead(context, Arrays.copyOfRange(frame, bodyLength + 2, frame.length));

        assertEquals(body, promise.getNow());
        assertFalse(handler.promiseMap.containsKey(channel));
    }

    @Test
    void retainsIncompleteNextFrame() throws Exception {
        String eof = "\r\n";
        handler = new JsonRpcTcpClientHandler(new TcpClientOption(eof, 1024));
        Promise<String> firstPromise = newPromise();

        handler.channelRead(context, ("first" + eof + "second").getBytes(StandardCharsets.UTF_8));
        assertEquals("first", firstPromise.getNow());

        Promise<String> secondPromise = newPromise();
        handler.channelRead(context, eof.getBytes(StandardCharsets.UTF_8));

        assertEquals("second", secondPromise.getNow());
        assertFalse(handler.promiseMap.containsKey(channel));
    }

    private Promise<String> newPromise() {
        Promise<String> promise = new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
        handler.promiseMap.put(channel, promise);
        return promise;
    }
}
