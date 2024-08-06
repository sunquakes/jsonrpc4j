package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcTcpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    @Value("${jsonrpc.server.netty.boss-group.thread-num:1}")
    private int bossGroupThreadNum;

    @Value("${jsonrpc.server.netty.worker-group.thread-num:0}")
    private int workerGroupThreadNum;

    private String packageEof;

    private int packageMaxLength;

    @Value("${jsonrpc.server.package-eof}")
    public void setPackageEof(String packageEof) {
        if (packageEof == null) packageEof = RequestUtils.TCP_PACKAGE_EOF;
        this.packageEof = packageEof;
    }

    @Value("${jsonrpc.server.package-max-length}")
    public void setPackageMaxLength(int packageMaxLength) {
        if (packageMaxLength == 0) packageMaxLength = RequestUtils.TCP_PACKAG_MAX_LENGHT;
        this.packageMaxLength = packageMaxLength;
    }

    public void start() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                EventLoopGroup bossGroup = new NioEventLoopGroup(bossGroupThreadNum);
                EventLoopGroup workerGroup = new NioEventLoopGroup(workerGroupThreadNum);

                ServerBootstrap sb = new ServerBootstrap();
                sb.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, poolMaxActive)
                        .option(ChannelOption.SO_RCVBUF, packageMaxLength)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {  // 绑定客户端连接时候触发操作
                            @Override
                            protected void initChannel(SocketChannel sh) throws Exception {
                                sh.pipeline()
                                        .addLast(new ByteArrayDecoder())
                                        .addLast(new ByteArrayEncoder())
                                        .addLast(new JsonRpcTcpServerHandler(applicationContext, new TcpServerOption(packageEof, packageMaxLength, new TcpServerPoolOption(poolMaxActive))));
                            }
                        });
                ChannelFuture future;
                future = sb.bind(port).sync();
                countDownLatch.countDown();

                if (future.isSuccess()) {
                    log.info("JsonRpc tcp server startup successfully.");
                } else {
                    log.info("JsonRpc tcp server startup failed.");
                    future.cause().printStackTrace();
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }

                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        countDownLatch.await();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
