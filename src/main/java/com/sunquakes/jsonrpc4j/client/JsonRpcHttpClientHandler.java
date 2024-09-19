package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
@Sharable
public class JsonRpcHttpClientHandler extends ChannelInboundHandlerAdapter {

    private final ConcurrentHashMap<Channel, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Synchronized
    public synchronized Queue<Object> send(JSONObject data, Channel channel) throws InterruptedException {
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        queueMap.put(channel, synchronousQueue);

        String message = data.toJSONString();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        ByteBuf buffer = request.content().clear();
        buffer.writerIndex();
        buffer.writeBytes(message.getBytes());
        buffer.writerIndex();
        buffer.readableBytes();
        request.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        request.headers().add(HttpHeaderNames.HOST, RequestUtils.getLocalIp());

        channel.writeAndFlush(request).sync();
        return synchronousQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        FullHttpResponse httpResponse = (FullHttpResponse) msg;
        ByteBuf buf = httpResponse.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        SynchronousQueue<Object> queue = queueMap.get(channel);
        queue.put(body);
        queueMap.remove(channel);
        httpResponse.release();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        handleInternalError(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handleInternalError(ctx);
    }

    private void handleInternalError(ChannelHandlerContext ctx) throws InterruptedException {
        Channel channel = ctx.channel();
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(null, RequestUtils.JSONRPC, new ErrorDto(ErrorEnum.INTERNAL_ERROR.getCode(), ErrorEnum.INTERNAL_ERROR.getText(), null));
        SynchronousQueue<Object> queue = queueMap.get(channel);
        queue.put(JSON.toJSONString(errorResponseDto));
        queueMap.remove(channel);
    }
}
