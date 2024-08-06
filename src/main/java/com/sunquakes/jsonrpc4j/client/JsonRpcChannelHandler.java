package com.sunquakes.jsonrpc4j.client;

import io.netty.channel.Channel;

import javax.net.ssl.SSLException;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 2.0.0
 * @since 1.0.0
 **/
public interface JsonRpcChannelHandler {
    void channelUpdated(Channel channel) throws SSLException;
}
