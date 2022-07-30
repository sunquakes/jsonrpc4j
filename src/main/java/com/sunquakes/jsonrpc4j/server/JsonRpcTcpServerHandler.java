package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ErrorResponseDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/6/28 9:30 PM
 **/
public class JsonRpcTcpServerHandler extends ChannelInboundHandlerAdapter {

    private ApplicationContext applicationContext;

    private TcpServerOption tcpServerOption;

    private ConcurrentHashMap<Channel, byte[]> bufferMap = new ConcurrentHashMap();

    public JsonRpcTcpServerHandler(ApplicationContext applicationContext, TcpServerOption tcpServerOption) {
        this.applicationContext = applicationContext;
        this.tcpServerOption = tcpServerOption;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        byte[] initBytes = bufferMap.getOrDefault(channel, new byte[0]);

        byte[] msgBytes = (byte[]) msg;

        String packageEof = tcpServerOption.getPackageEof();
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
                JsonRpcServerHandler jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
                Object res = jsonRpcServerHandler.handle(new String(bytes));
                byte[] output = ByteArrayUtils.merge(JSON.toJSONBytes(res), packageEof.getBytes());
                ctx.writeAndFlush(output);
                bytes = initBytes;
            }
        }
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
