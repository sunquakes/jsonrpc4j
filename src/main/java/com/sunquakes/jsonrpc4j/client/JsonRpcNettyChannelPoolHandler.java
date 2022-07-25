package com.sunquakes.jsonrpc4j.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/25 6:47 PM
 **/
public class JsonRpcNettyChannelPoolHandler implements ChannelPoolHandler {

    JsonRpcNettyChannelHandler handler;

    JsonRpcNettyChannelPoolHandler(JsonRpcNettyChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        handler.channelUpdated(ch);
    }
}
