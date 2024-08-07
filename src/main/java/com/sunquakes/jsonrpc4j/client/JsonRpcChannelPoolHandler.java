package com.sunquakes.jsonrpc4j.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 2.0.0
 * @since 1.0.0
 **/
public class JsonRpcChannelPoolHandler implements ChannelPoolHandler {

    JsonRpcChannelHandler handler;

    JsonRpcChannelPoolHandler(JsonRpcChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
        // There is no needs to handle channel acquired event.
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        handler.channelUpdated(ch);
    }
}
