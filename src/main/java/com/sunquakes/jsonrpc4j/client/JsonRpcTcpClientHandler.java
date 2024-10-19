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
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
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

    private final TcpClientOption tcpClientOption;

    public JsonRpcTcpClientHandler(TcpClientOption tcpClientOption) {
        this.tcpClientOption = tcpClientOption;
    }

    @Synchronized
    public synchronized String send(RequestDto request, Channel channel) throws InterruptedException, ExecutionException {
        String message = JSONUtils.toString(request) + tcpClientOption.getPackageEof();
        ByteBuf byteBuf = channel.alloc().buffer(tcpClientOption.getPackageMaxLength());
        byteBuf.writeBytes(message.getBytes());

        Promise<String> promise = new DefaultPromise<>(channel.eventLoop());
        promiseMap.put(channel, promise);

        channel.writeAndFlush(byteBuf).sync();
        return promise.sync().get();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        byte[] initBytes = bufferMap.getOrDefault(channel, new byte[0]);

        byte[] msgBytes = (byte[]) msg;

        String packageEof = tcpClientOption.getPackageEof();
        int packageEofBytesLength = packageEof.length();
        byte[] packageEofBytes = packageEof.getBytes();

        int index = initBytes.length;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(initBytes);
        byteArrayOutputStream.write(msgBytes);
        byte[] bytes = byteArrayOutputStream.toByteArray();

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
                String body = new String(bytes);
                synchronized (channel) {
                    Promise<String> promise = promiseMap.get(channel);
                    if (promise != null) {
                        promise.setSuccess(body);
                    }
                }
                bytes = initBytes;
            }
        }
    }
}
