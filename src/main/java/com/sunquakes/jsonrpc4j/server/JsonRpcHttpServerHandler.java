package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.context.ApplicationContext;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
public class JsonRpcHttpServerHandler extends ChannelInboundHandlerAdapter {

    private ApplicationContext applicationContext;

    public JsonRpcHttpServerHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        HttpVersion httpVersion = httpRequest.protocolVersion();

        ByteBuf buf = httpRequest.content();
        HttpMethod method = httpRequest.method();
        if (HttpMethod.GET.equals(method)) {
            send(ctx, "", HttpResponseStatus.OK, httpVersion);
            return;
        }
        if (!HttpMethod.POST.equals(method)) {
            send(ctx, "", HttpResponseStatus.METHOD_NOT_ALLOWED, httpVersion);
            return;
        }

        String body = buf.toString(CharsetUtil.UTF_8);

        JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
        Object res = jsonRpcServerHandler.handle(body);
        String output = JSONUtils.toString(res);

        send(ctx, output, HttpResponseStatus.OK, httpVersion);
        httpRequest.release();
    }

    private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status, HttpVersion httpVersion) {
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
