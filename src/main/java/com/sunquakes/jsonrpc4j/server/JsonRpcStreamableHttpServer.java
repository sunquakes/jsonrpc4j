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
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Netty server for the MCP Streamable HTTP transport.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
@Slf4j
public class JsonRpcStreamableHttpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    @Value("${jsonrpc.server.netty.boss-group.thread-num:1}")
    private int bossGroupThreadNum;

    @Value("${jsonrpc.server.netty.worker-group.thread-num:0}")
    private int workerGroupThreadNum;

    private StreamableHttpServerHandler handler;

    public void start() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        StreamableHttpServerHandler finalHandler = handler;

        executorService.submit(() -> {
            try {
                EventLoopGroup bossGroup = new NioEventLoopGroup(bossGroupThreadNum);
                EventLoopGroup workerGroup = new NioEventLoopGroup(workerGroupThreadNum);

                ServerBootstrap sb = new ServerBootstrap();
                sb.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, poolMaxActive)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel sh) {
                                sh.pipeline()
                                        .addLast("codec", new HttpServerCodec())
                                        .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                                        .addLast(finalHandler);
                            }
                        });
                ChannelFuture future;
                future = sb.bind(port).sync();
                countDownLatch.countDown();

                if (future.isSuccess()) {
                    log.info("JsonRpc streamable http server startup successfully.");
                } else {
                    log.info("JsonRpc streamable http server startup failed.");
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }

                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        });
        countDownLatch.await();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        handler = new StreamableHttpServerHandler(applicationContext);
        start();
    }
}
