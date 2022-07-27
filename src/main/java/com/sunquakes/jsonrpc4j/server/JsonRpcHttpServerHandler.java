package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.context.ApplicationContext;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/2 12:32 PM
 **/
public class JsonRpcHttpServerHandler extends ChannelInboundHandlerAdapter {

    private ApplicationContext applicationContext;

    public JsonRpcHttpServerHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest httpRequest = (FullHttpRequest) msg;

        ByteBuf buf = httpRequest.content();
        HttpMethod method = httpRequest.method();
        if (!HttpMethod.POST.equals(method)) {
            send(ctx, "", HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        String body = buf.toString(CharsetUtil.UTF_8);

        JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
        Object res = jsonRpcServerHandler.handle(body);
        String output = JSON.toJSONString(res);

        send(ctx, output, HttpResponseStatus.OK);
        httpRequest.release();
    }

    private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
