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
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/6/28 9:05 PM
 **/
@Slf4j
public class JsonRpcNettyServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

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
                            .childHandler(new ChannelInitializer<SocketChannel>() {  // 绑定客户端连接时候触发操作
                                @Override
                                protected void initChannel(SocketChannel sh) throws Exception {
                                    sh.pipeline()
                                            .addLast(new ByteArrayDecoder())
                                            .addLast(new ByteArrayEncoder())
                                            .addLast(new JsonRpcNettyServerHandler(applicationContext, new TcpServerOption(packageEof, packageMaxLength, new TcpServerPoolOption(poolMaxActive)), new byte[0]));
                                }
                            });
                    ChannelFuture future = null;
                    future = sb.bind(port).sync();
                    countDownLatch.countDown();

                    if (future.isSuccess()) {
                        System.out.println("Server startup successfully.");
                    } else {
                        System.out.println("Server startup failed.");
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
