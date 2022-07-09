package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.server.JsonRpcServerHandler;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/8 12:39 PM
 **/
@Slf4j
@Sharable
public class JsonRpcNettyHttpClientHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Synchronized
    public synchronized SynchronousQueue<Object> send(JSONObject data, Channel channel) {
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        queueMap.put(data.getString("id"), synchronousQueue);

        String message = data.toJSONString();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        ByteBuf buffer = request.content().clear();
        buffer.writerIndex();
        buffer.writeBytes(message.getBytes());
        buffer.writerIndex();
        buffer.readableBytes();
        request.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

        channel.writeAndFlush(request);
        channel.closeFuture();
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
        httpResponse.release();
    }
}
