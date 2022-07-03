package com.sunquakes.jsonrpc4j.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/3 2:44 PM
 **/
@Slf4j
public class JsonRpcNettyTcpClientHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext channelHandlerContext;

    private ChannelPromise channelPromise;

    private byte[] data;

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channelHandlerContext = channelHandlerContext;
    }

    public ChannelPromise send(String message) {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer(4 * message.length());
        byteBuf.writeBytes(message.getBytes());
        channelPromise = channelHandlerContext.writeAndFlush(byteBuf).channel().newPromise();
        return channelPromise;
    }

    public byte[] getData() {
        return data;
    }
}
