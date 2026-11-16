package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
@Sharable
public class JsonRpcTcpClientHandler extends JsonRpcClientHandler {

    private final ConcurrentHashMap<Channel, byte[]> bufferMap = new ConcurrentHashMap<>();

    private final byte[] packageEofBytes;

    private final int packageEofBytesLength;

    public JsonRpcTcpClientHandler(TcpClientOption tcpClientOption) {
        this.packageEofBytes = tcpClientOption.getPackageEof().getBytes(StandardCharsets.UTF_8);
        this.packageEofBytesLength = packageEofBytes.length;
    }

    public String send(RequestDto request, Channel channel) throws InterruptedException, ExecutionException {
        byte[] body = JSONUtils.toBytes(request);
        ByteBuf byteBuf = channel.alloc().buffer(body.length + packageEofBytesLength);
        byteBuf.writeBytes(body).writeBytes(packageEofBytes);

        Promise<String> promise = new DefaultPromise<>(channel.eventLoop());
        promiseMap.put(channel, promise);

        channel.writeAndFlush(byteBuf).sync();
        return promise.sync().get();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        byte[] initBytes = bufferMap.getOrDefault(channel, new byte[0]);

        byte[] bytes = ByteArrayUtils.merge(initBytes, (byte[]) msg);

        int index = initBytes.length;

        if (index < packageEofBytesLength) {
            index = packageEofBytesLength;
        }
        while (true) {
            int i = ByteArrayUtils.strstr(bytes, packageEofBytes, index - packageEofBytesLength);
            if (i != -1) {
                if (i + packageEofBytesLength < bytes.length) {
                    initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
                } else {
                    initBytes = new byte[0];
                }
                bytes = Arrays.copyOfRange(bytes, 0, i);
            } else {
                initBytes = bytes;
                bufferMap.put(channel, initBytes);
                break;
            }
            if (bytes.length > 0) {
                String body = new String(bytes, StandardCharsets.UTF_8);
                Promise<String> promise = promiseMap.remove(channel);
                if (promise != null) {
                    promise.setSuccess(body);
                }
                bytes = initBytes;
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        bufferMap.remove(ctx.channel());
        super.channelUnregistered(ctx);
    }
}
