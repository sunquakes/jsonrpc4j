package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/3 2:44 PM
 **/
@Slf4j
@Sharable
public class JsonRpcNettyTcpClientHandler extends ChannelInboundHandlerAdapter {

    private byte[] data;

    private byte[] initBytes = new byte[0];

    private TcpClientOption tcpClientOption;

    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    ChannelHandlerContext channelHandlerContext;

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channelHandlerContext = channelHandlerContext;
    }

    public synchronized SynchronousQueue<Object> send(JSONObject request, Channel channel) {
        String message = request.toJSONString() + tcpClientOption.getPackageEof();
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer(4 * message.length());
        byteBuf.writeBytes(message.getBytes());
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        queueMap.put(request.getString("id"), synchronousQueue);
        channelHandlerContext.writeAndFlush(byteBuf);
        return synchronousQueue;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
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
                break;
            }
            if (bytes.length > 0) {
                this.data = bytes;
                String body = new String(bytes);
                ResponseDto responseDto = JSONObject.parseObject(body, ResponseDto.class);
                String id = responseDto.getId();
                SynchronousQueue<Object> queue = queueMap.get(id);
                queue.put(body);
                queueMap.remove(id);
                bytes = initBytes;
            }
        }
    }

    public JsonRpcNettyTcpClientHandler setOption(TcpClientOption tcpClientOption) {
        this.tcpClientOption = tcpClientOption;
        return this;
    }
}
