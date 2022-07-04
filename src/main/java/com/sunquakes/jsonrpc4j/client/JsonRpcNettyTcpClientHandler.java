package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/3 2:44 PM
 **/
@Slf4j
@Sharable
public class JsonRpcNettyTcpClientHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext channelHandlerContext;

    private ChannelPromise channelPromise;

    private byte[] data;

    private byte[] initBytes = new byte[0];

    private TcpClientOption tcpClientOption;

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channelHandlerContext = channelHandlerContext;
    }

    public synchronized ChannelPromise send(String message) {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer(4 * message.length());
        byteBuf.writeBytes(message.getBytes());
        channelPromise = channelHandlerContext.writeAndFlush(byteBuf).channel().newPromise();
        return channelPromise;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        byte[] msgBytes = (byte[]) msg;

        String packageEof = "\r\n";
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
                System.out.println(new String(bytes));
                bytes = initBytes;
                channelPromise.setSuccess();
            }
        }
    }

    public JsonRpcNettyTcpClientHandler setOption(TcpClientOption tcpClientOption) {
        this.tcpClientOption = tcpClientOption;
        return this;
    }
}
