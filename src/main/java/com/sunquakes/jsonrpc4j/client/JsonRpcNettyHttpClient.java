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
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
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

    private String protocol;

    private String url;

    private String IP;

    private Integer PORT;

    private JsonRpcNettyHttpClientHandler jsonRpcNettyHttpClientHandler;

    private static ConcurrentHashMap bootstrapMap = new ConcurrentHashMap();

    public JsonRpcNettyHttpClient(String protocol, String url) {
        jsonRpcNettyHttpClientHandler = new JsonRpcNettyHttpClientHandler();
        this.protocol = protocol;
        this.url = url;
        Object[] ipPort = getIpPort(protocol, url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        ObjectPool<Channel> pool = getPool();
        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);
        Channel channel = pool.borrowObject();
        SynchronousQueue<Object> queue = jsonRpcNettyHttpClientHandler.send(request, channel);
        String body = (String) queue.take();
        pool.returnObject(channel);
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

    @Synchronized
    private ObjectPool<Channel> getPool() throws Exception {
        Bootstrap bootstrap = (Bootstrap) bootstrapMap.get(url);
        if (bootstrap == null) {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(new InetSocketAddress(IP, PORT))
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("codec", new HttpClientCodec())
                                    .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                                    .addLast(jsonRpcNettyHttpClientHandler);
                            if (protocol.equals(RequestUtils.PROTOCOL_HTTPS)) {
                                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                                ch.pipeline().addLast(new OptionalSslHandler(sslContext));
                            }
                        }
                    });
        }
        ObjectPool<Channel> pool = JsonRpcNettyChannelPoolFactory.getPool(url, new JsonRpcNettyChannelFactory(bootstrap));
        return pool;
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
