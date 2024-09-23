package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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
public class JsonRpcTcpClientHandler extends ChannelInboundHandlerAdapter {

    private final ConcurrentHashMap<Channel, byte[]> bufferMap = new ConcurrentHashMap<>();

    private final TcpClientOption tcpClientOption;

    private final ConcurrentHashMap<Channel, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    public JsonRpcTcpClientHandler(TcpClientOption tcpClientOption) {
        this.tcpClientOption = tcpClientOption;
    }

    @Synchronized
    public synchronized Queue<Object> send(JSONObject request, Channel channel) {
        String message = request.toJSONString() + tcpClientOption.getPackageEof();
        ByteBuf byteBuf = channel.alloc().buffer(tcpClientOption.getPackageMaxLength());
        byteBuf.writeBytes(message.getBytes());
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        queueMap.put(channel, synchronousQueue);
        channel.writeAndFlush(byteBuf);
        return synchronousQueue;
    }

    @Override
    @Synchronized
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
                    SynchronousQueue<Object> queue = queueMap.get(channel);
                    if (queue != null) {
                        queue.put(body);
                    }
                }
                bytes = initBytes;
            }
        }
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
