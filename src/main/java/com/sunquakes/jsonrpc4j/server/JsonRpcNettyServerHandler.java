package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson.JSON;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/6/28 9:30 PM
 **/
public class JsonRpcNettyServerHandler extends ChannelInboundHandlerAdapter {

    private ApplicationContext applicationContext;

    private TcpServerOption tcpServerOption;

    private byte[] initBytes = new byte[0];

    public JsonRpcNettyServerHandler(ApplicationContext applicationContext, TcpServerOption tcpServerOption) {
        this.applicationContext = applicationContext;
        this.tcpServerOption = tcpServerOption;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

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
}
