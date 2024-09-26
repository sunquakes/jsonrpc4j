package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
@Sharable
public class JsonRpcHttpClientHandler extends JsonRpcClientHandler {

    @Synchronized
    public synchronized String send(JSONObject data, Channel channel) throws InterruptedException, ExecutionException {
        Promise<String> promise = new DefaultPromise<>(channel.eventLoop());
        promiseMap.put(channel, promise);

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
        return promise.sync().get();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        FullHttpResponse httpResponse = (FullHttpResponse) msg;
        ByteBuf buf = httpResponse.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        Promise<String> promise = promiseMap.get(channel);
        promise.setSuccess(body);
        promiseMap.remove(channel);
        httpResponse.release();
    }
}
