package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/8 12:39 PM
 **/
@Slf4j
public class JsonRpcHttpClient implements JsonRpcClientInterface {

    private Integer DEFAULT_HTTP_PORT = 80;

    private Integer DEFAULT_HTTPS_PORT = 443;

    private String protocol;

    private InetSocketAddress address;

    private JsonRpcHttpClientHandler jsonRpcHttpClientHandler;

    private static ConcurrentHashMap bootstrapMap = new ConcurrentHashMap();

    public JsonRpcHttpClient(String protocol, String url) {
        jsonRpcHttpClientHandler = new JsonRpcHttpClientHandler();
        this.protocol = protocol;
        Object[] ipPort = getIpPort(protocol, url);
        String ip = (String) ipPort[0];
        Integer port = (Integer) ipPort[1];
        this.address = new InetSocketAddress(ip, port);
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);
        ResponseDto responseDto;
        String body;
        try {
            FixedChannelPool pool = getPool();
            Channel channel = pool.acquire().get();
            SynchronousQueue<Object> queue = jsonRpcHttpClientHandler.send(request, channel);
            body = (String) queue.take();
            pool.release(channel);
            responseDto = JSONObject.parseObject(body, ResponseDto.class);
        } catch (UndeclaredThrowableException e) {
            throw new JsonRpcClientException(e.getMessage());
        }
        if (responseDto.getResult() == null) {
            JSONObject bodyJSON = JSON.parseObject(body);
            if (bodyJSON.containsKey("error")) {
                ErrorDto errorDto = JSONObject.parseObject(bodyJSON.getString("error"), ErrorDto.class);
                throw ErrorEnum.getException(errorDto.getCode(), errorDto.getMessage());
            }
        }
        return responseDto.getResult();
    }

    @Synchronized
    private FixedChannelPool getPool() throws Exception {
        JsonRpcChannelPoolHandler handler = new JsonRpcChannelPoolHandler(new Handler());
        Bootstrap bootstrap = (Bootstrap) bootstrapMap.get(this.address);
        if (bootstrap == null) {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(this.address)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            bootstrapMap.put(this.address, bootstrap);
        }
        FixedChannelPool pool = JsonRpcChannelPoolFactory.getPool(this.address, bootstrap, handler);
        return pool;
    }

    class Handler implements JsonRpcChannelHandler {
        @Override
        public void channelUpdated(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast("codec", new HttpClientCodec())
                    .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                    .addLast(jsonRpcHttpClientHandler);
            if (protocol.equals(RequestUtils.PROTOCOL_HTTPS)) {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addLast(new OptionalSslHandler(sslContext));
            }
        }
    }

    private Object[] getIpPort(String protocol, String url) {
        String[] ipPort = url.split(":");
        String ip = ipPort[0];
        Integer port;
        if (ipPort.length < 2) {
            if (protocol.equals(RequestUtils.PROTOCOL_HTTPS)) {
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