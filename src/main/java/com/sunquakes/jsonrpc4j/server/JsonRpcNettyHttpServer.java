package com.sunquakes.jsonrpc4j.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/7/2 12:32 PM
 **/
@Slf4j
public class JsonRpcNettyHttpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    public void start() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    EventLoopGroup bossGroup = new NioEventLoopGroup();
                    EventLoopGroup workerGroup = new NioEventLoopGroup();

                    ServerBootstrap sb = new ServerBootstrap();
                    sb.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, poolMaxActive)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel sh) throws Exception {
                                    sh.pipeline()
                                            .addLast("http-decoder", new HttpRequestDecoder())
                                            .addLast("http-encoder", new HttpResponseEncoder())
                                            .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                                            .addLast(new JsonRpcNettyHttpServerHandler(applicationContext));
                                }
                            });
                    ChannelFuture future;
                    future = sb.bind(port).sync();
                    countDownLatch.countDown();

                    if (future.isSuccess()) {
                        log.info("JsonRpc http server startup successfully.");
                    } else {
                        log.info("JsonRpc http server startup failed.");
                        future.cause().printStackTrace();
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    }

                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        countDownLatch.await();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
