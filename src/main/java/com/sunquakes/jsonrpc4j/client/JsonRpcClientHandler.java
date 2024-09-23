package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.0.0
 * @since 3.0.0
 **/
@Slf4j
@Sharable
public abstract class JsonRpcClientHandler extends ChannelInboundHandlerAdapter {

    protected final ConcurrentHashMap<Channel, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        handleInternalError(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handleInternalError(ctx);
    }

    protected void handleInternalError(ChannelHandlerContext ctx) throws InterruptedException {
        Channel channel = ctx.channel();
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(null, RequestUtils.JSONRPC, new ErrorDto(ErrorEnum.INTERNAL_ERROR.getCode(), ErrorEnum.INTERNAL_ERROR.getText(), null));
        SynchronousQueue<Object> queue = queueMap.get(channel);
        queue.put(JSON.toJSONString(errorResponseDto));
        queueMap.remove(channel);
    }
}
