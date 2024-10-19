package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.0.0
 * @since 3.0.0
 **/
@Slf4j
@Sharable
public abstract class JsonRpcClientHandler extends ChannelInboundHandlerAdapter {

    protected Map<Channel, Promise<String>> promiseMap = new ConcurrentHashMap<>();

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        handleInternalError(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handleInternalError(ctx);
    }

    protected void handleInternalError(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(null, RequestUtils.JSONRPC, new ErrorDto(ErrorEnum.INTERNAL_ERROR.getCode(), ErrorEnum.INTERNAL_ERROR.getText(), null));
        Promise<String> promise = promiseMap.get(channel);
        if (promise != null) {
            promise.setSuccess(JSONUtils.toString(errorResponseDto));
            promiseMap.remove(channel);
        }
    }
}
