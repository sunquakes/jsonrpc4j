package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.config.Config;
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
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/8 12:39 PM
 **/
@Slf4j
public class JsonRpcHttpClient extends JsonRpcClient implements JsonRpcClientInterface {

    private static int DEFAULT_HTTP_PORT = 80;

    private static int DEFAULT_HTTPS_PORT = 443;

    private JsonRpcHttpClientHandler jsonRpcHttpClientHandler = new JsonRpcHttpClientHandler();

    public JsonRpcHttpClient(Config config) {
        super(config);
    }

    @Override
    public void initLoadBalancer() {
        JsonRpcChannelPoolHandler poolHandler = new JsonRpcChannelPoolHandler(new Handler());
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
        int defaultPort = protocol.equals(JsonRpcProtocol.https) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
        if (discovery != null) {
            loadBalancer = new JsonRpcLoadBalancer(() -> discovery.value().get(name), defaultPort, bootstrap, poolHandler);
        } else {
            loadBalancer = new JsonRpcLoadBalancer(() -> url.value(), defaultPort, bootstrap, poolHandler);
        }
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
        FixedChannelPool pool = loadBalancer.getPool();
        try {
            Channel channel = pool.acquire().get();
            SynchronousQueue<Object> queue = jsonRpcHttpClientHandler.send(request, channel);
            body = (String) queue.take();
            pool.release(channel);
            responseDto = JSONObject.parseObject(body, ResponseDto.class);
        } catch (Exception e) {
            loadBalancer.removePool(pool);
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
}