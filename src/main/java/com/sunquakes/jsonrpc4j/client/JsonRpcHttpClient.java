package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcHttpClient extends JsonRpcClient implements JsonRpcClientInterface {

    private static final int DEFAULT_HTTP_PORT = 80;

    private static final int DEFAULT_HTTPS_PORT = 443;

    private final JsonRpcHttpClientHandler jsonRpcHttpClientHandler = new JsonRpcHttpClientHandler();

    public JsonRpcHttpClient(Config<Object> config) {
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
        int defaultPort = isHttps(protocol) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
        if (discovery != null) {
            loadBalancer = new JsonRpcLoadBalancer(() -> discovery.value().get(name), defaultPort, bootstrap, poolHandler);
        } else {
            loadBalancer = new JsonRpcLoadBalancer(() -> url.value(), defaultPort, bootstrap, poolHandler);
        }
    }

    @Override
    public Object handle(String method, Object[] args) throws JsonRpcException {
        return handle(method, args, jsonRpcHttpClientHandler);
    }

    class Handler implements JsonRpcChannelHandler {
        @Override
        public void channelUpdated(Channel ch) throws SSLException {
            SocketChannel sc = (SocketChannel) ch;
            if (isHttps(protocol)) {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
            }
            sc.pipeline()
                    .addLast("codec", new HttpClientCodec())
                    .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024));
            sc.pipeline().addLast(jsonRpcHttpClientHandler);
        }
    }

    private boolean isHttps(String scheme) {
        return JsonRpcProtocol.HTTPS.name().equalsIgnoreCase(scheme);
    }
}