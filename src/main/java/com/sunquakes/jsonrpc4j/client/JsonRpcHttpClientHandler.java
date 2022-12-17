package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
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

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/8 12:39 PM
 **/
@Slf4j
@Sharable
public class JsonRpcHttpClientHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Channel, ConcurrentHashMap<String, Integer>> channelQueueMap = new ConcurrentHashMap();

    @Synchronized
    public synchronized SynchronousQueue<Object> send(JSONObject data, Channel channel) throws InterruptedException {
        String id = data.getString("id");
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        queueMap.put(id, synchronousQueue);
        ConcurrentHashMap<String, Integer> idMap = channelQueueMap.getOrDefault(channel, new ConcurrentHashMap<>());
        idMap.put(id, 0);
        channelQueueMap.putIfAbsent(channel, idMap);

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
        FullHttpResponse httpResponse = (FullHttpResponse) msg;
        ByteBuf buf = httpResponse.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        ResponseDto responseDto = JSONObject.parseObject(body, ResponseDto.class);
        String id = responseDto.getId();
        SynchronousQueue<Object> queue = queueMap.get(id);
        queue.put(body);
        queueMap.remove(id);
        ConcurrentHashMap<String, Integer> idMap = channelQueueMap.get(ctx.channel());
        idMap.remove(id, 0);
        httpResponse.release();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ConcurrentHashMap<String, Integer> idMap = channelQueueMap.get(ctx.channel());
        if (idMap == null) return;
        for (String id : idMap.keySet()) {
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(id, RequestUtils.JSONRPC, new ErrorDto(ErrorEnum.InternalError.getCode(), ErrorEnum.InternalError.getText(), null));
            synchronized (id) {
                SynchronousQueue<Object> queue = queueMap.get(id);
                if (queue != null) {
                    queue.put(JSON.toJSONString(errorResponseDto));
                    idMap.remove(id);
                    queueMap.remove(id);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ConcurrentHashMap<String, Integer> idMap = channelQueueMap.get(ctx.channel());
        if (idMap == null) return;
        for (String id : idMap.keySet()) {
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(id, RequestUtils.JSONRPC, new ErrorDto(ErrorEnum.InternalError.getCode(), ErrorEnum.InternalError.getText(), null));
            synchronized (id) {
                SynchronousQueue<Object> queue = queueMap.get(id);
                if (queue != null) {
                    queue.put(JSON.toJSONString(errorResponseDto));
                    idMap.remove(id);
                    queueMap.remove(id);
                }
            }
        }
    }
}
