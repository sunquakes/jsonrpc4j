package com.sunquakes.jsonrpc4j.discovery;

import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.0.0
 * @since 3.0.0
 **/
public class NettyHttpClient {

    private static final int DEFAULT_HTTP_PORT = 80;

    private static final int DEFAULT_HTTPS_PORT = 443;

    private URI uri;

    NettyHttpClient(String host) {
        uri = URI.create(host);
    }

    public FullHttpResponse get(String path) throws ExecutionException, InterruptedException {
        return request(path, HttpMethod.GET);
    }

    public FullHttpResponse post(String path) throws ExecutionException, InterruptedException {
        return request(path, HttpMethod.POST);
    }

    public FullHttpResponse put(String path) throws ExecutionException, InterruptedException {
        return request(path, HttpMethod.PUT);
    }

    private FullHttpResponse request(String path, HttpMethod method) throws InterruptedException, ExecutionException {
        EventLoopGroup group = new NioEventLoopGroup();
        Promise<FullHttpResponse> promise = new DefaultPromise<>(group.next());
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new HttpClientInitializer(promise));
        int port = uri.getPort();
        int defaultPort = isHttps(uri.getScheme()) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
        if (port == -1) {
            port = defaultPort;
        }

        Channel channel = bootstrap.connect(new InetSocketAddress(uri.getHost(), port)).sync().channel();

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, path, Unpooled.EMPTY_BUFFER);

        channel.writeAndFlush(request).sync();
        return promise.sync().get();
    }

    private boolean isHttps(String scheme) {
        return scheme.toUpperCase().equals(JsonRpcProtocol.HTTPS.name());
    }

    class HttpClientInitializer extends ChannelInitializer<Channel> {

        Promise<FullHttpResponse> promise;

        HttpClientInitializer(Promise<FullHttpResponse> promise) {
            this.promise = promise;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            SocketChannel sc = (SocketChannel) ch;
            sc.config().setKeepAlive(true);
            sc.config().setTcpNoDelay(true);
            if (isHttps(uri.getScheme())) {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
            }
            sc.pipeline()
                    .addLast("codec", new HttpClientCodec())
                    .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024));
            sc.pipeline().addLast(new HttpResponseHandler(promise));
        }
    }

    class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        Promise<FullHttpResponse> promise;

        HttpResponseHandler(Promise<FullHttpResponse> promise) {
            this.promise = promise;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
            FullHttpResponse httpResponse = msg;
            promise.setSuccess(httpResponse);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            promise.setFailure(cause);
            ctx.close();
        }
    }
}