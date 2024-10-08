package com.sunquakes.jsonrpc4j.client;

import io.netty.channel.Channel;

import javax.net.ssl.SSLException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
public interface JsonRpcChannelHandler {
    void channelUpdated(Channel channel) throws SSLException;
}
