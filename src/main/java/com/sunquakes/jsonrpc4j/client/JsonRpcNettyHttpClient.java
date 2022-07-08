package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/8 12:39 PM
 **/
@Slf4j
public class JsonRpcNettyHttpClient implements JsonRpcClientHandlerInterface {

    private Integer DEFAULT_HTTP_PORT = 80;

    private Integer DEFAULT_HTTPS_PORT = 443;

    private String url;

    private String IP;

    private Integer PORT;

    private JsonRpcNettyHttpClientHandler jsonRpcNettyHttpClientHandler;

    public JsonRpcNettyHttpClient(String protocol, String url) {
        jsonRpcNettyHttpClientHandler = new JsonRpcNettyHttpClientHandler();
        this.url = url;
        Object[] ipPort = getIpPort(protocol, url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .remoteAddress(new InetSocketAddress(IP, PORT))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("http-decoder", new HttpResponseDecoder())
                                .addLast("http-encoder", new HttpRequestEncoder())
                                .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                                .addLast(jsonRpcNettyHttpClientHandler);
                    }
                });

        ObjectPool<Channel> pool = JsonRpcNettyChannelPoolFactory.getPool(url, new JsonRpcNettyChannelFactory(bootstrap));
        Channel channel = pool.borrowObject();

        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);

        SynchronousQueue<Object> queue = jsonRpcNettyHttpClientHandler.send(request, channel);
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

    private Object[] getIpPort(String protocol, String url) {
        String[] ipPort = url.split(":");
        String ip = ipPort[0];
        Integer port;
        if (ipPort.length < 2) {
            if (protocol == "https") {
                port = DEFAULT_HTTPS_PORT;
            } else {
                port = DEFAULT_HTTP_PORT;
            }
        } else {
            port = Integer.valueOf(ipPort[1]);
        }
        return new Object[]{ip, port};
    }
}
