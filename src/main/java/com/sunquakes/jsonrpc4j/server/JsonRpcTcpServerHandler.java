package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
public class JsonRpcTcpServerHandler extends ChannelInboundHandlerAdapter {

    private final JsonRpcServerHandler jsonRpcServerHandler;

    private final byte[] packageEofBytes;

    private final int packageEofBytesLength;

    private final ConcurrentHashMap<Channel, byte[]> bufferMap = new ConcurrentHashMap<>();

    public JsonRpcTcpServerHandler(ApplicationContext applicationContext, TcpServerOption tcpServerOption) {
        this.jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
        this.packageEofBytes = tcpServerOption.getPackageEof().getBytes(StandardCharsets.UTF_8);
        this.packageEofBytesLength = packageEofBytes.length;
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
                Object res = jsonRpcServerHandler.handle(new String(bytes, StandardCharsets.UTF_8));
                byte[] output = ByteArrayUtils.merge(JSONUtils.toBytes(res), packageEofBytes);
                ctx.writeAndFlush(output);
                bytes = initBytes;
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        bufferMap.remove(ctx.channel());
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
