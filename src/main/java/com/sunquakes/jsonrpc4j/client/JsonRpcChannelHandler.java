package com.sunquakes.jsonrpc4j.client;

import io.netty.channel.Channel;

import javax.net.ssl.SSLException;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/25 6:47 PM
 **/
public interface JsonRpcChannelHandler {
    void channelUpdated(Channel channel) throws SSLException;
}
