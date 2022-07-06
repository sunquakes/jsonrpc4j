package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.SynchronousQueue;

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

    private TcpClientOption tcpClientOption;

    public JsonRpcNettyTcpClient(String url, TcpClientOption tcpClientOption) {
        this.tcpClientOption = tcpClientOption;
        Object[] ipPort = getIpPort(url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        JsonRpcNettyTcpClientHandler jsonRpcNettyTcpClientHandler = new JsonRpcNettyTcpClientHandler(tcpClientOption);

        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
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
        Channel channel = bootstrap.connect(IP, PORT).sync().channel();

        SynchronousQueue<Object> queue = jsonRpcNettyTcpClientHandler.send(request, channel);
        String body = (String) queue.take();
        ResponseDto responseDto = JSONObject.parseObject(body, ResponseDto.class);
        if (responseDto.getResult() == null) {
            JSONObject bodyJSON = JSON.parseObject(body);
            if (bodyJSON.containsKey("error")) {
                ErrorDto errorDto = JSONObject.parseObject(bodyJSON.getString("error"), ErrorDto.class);
                throw ErrorEnum.getException(errorDto.getCode(), errorDto.getMessage());
            }
        }
        return responseDto.getResult();
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
