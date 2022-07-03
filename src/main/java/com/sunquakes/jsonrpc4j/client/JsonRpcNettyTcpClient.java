package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/6/28 9:05 PM
 **/
@Slf4j
public class JsonRpcNettyTcpClient implements JsonRpcClientHandlerInterface {

    private Integer DEFAULT_PORT = 80;

    private String IP;

    private Integer PORT;

    private JsonRpcNettyTcpClientHandler jsonRpcNettyTcpClientHandler = new JsonRpcNettyTcpClientHandler();

    private TcpClientOption option;

    private String url;

    public JsonRpcNettyTcpClient(String url) {
        this.url = url;
        Object[] ipPort = getIpPort(url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        String packageEof = option.getPackageEof();

        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);
        String msg = request + packageEof;

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ByteArrayDecoder())
                                    .addLast(new ByteArrayEncoder())
                                    .addLast(jsonRpcNettyTcpClientHandler);
                        }
                    });
            bootstrap.connect(IP, PORT).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ChannelPromise channelPromise = jsonRpcNettyTcpClientHandler.send(msg);
        channelPromise.await();
        return jsonRpcNettyTcpClientHandler.getData();
    }

    public JsonRpcNettyTcpClient setOption(TcpClientOption option) {
        this.option = option;
        return this;
    }

    private Object[] getIpPort(String url) {
        String[] ipPort = url.split(":");
        String ip = ipPort[0];
        Integer port;
        if (ipPort.length < 2) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.valueOf(ipPort[1]);
        }
        return new Object[]{ip, port};
    }
}
