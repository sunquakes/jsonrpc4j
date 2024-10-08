package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
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
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcHttpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.ssl.key-store:#{null}}")
    private String sslKeyStore;

    @Value("${jsonrpc.server.ssl.key-store-type:#{null}}")
    private String sslKeyStoreType;

    @Value("${jsonrpc.server.ssl.key-store-password:#{null}}")
    private String sslKeyStorePassword;

    @Value("${jsonrpc.server.protocol}")
    private String protocol;

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    @Value("${jsonrpc.server.netty.boss-group.thread-num:1}")
    private int bossGroupThreadNum;

    @Value("${jsonrpc.server.netty.worker-group.thread-num:0}")
    private int workerGroupThreadNum;

    public void start() throws InterruptedException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, CertificateException {
        SslContext sslContext = null;
        protocol = protocol.toUpperCase();
        if (protocol.equals(JsonRpcProtocol.HTTPS.name())) {
            InputStream sslInputStream = getClass().getClassLoader().getResourceAsStream(sslKeyStore);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(sslKeyStoreType);
            keyStore.load(sslInputStream, sslKeyStorePassword.toCharArray());
            keyManagerFactory.init(keyStore, sslKeyStorePassword.toCharArray());
            sslContext = SslContextBuilder.forServer(keyManagerFactory).build();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        final SslContext finalSslContext = sslContext;

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
                                protocol = protocol.toUpperCase();
                                if (protocol.equals(JsonRpcProtocol.HTTPS.name())) {
                                    sh.pipeline().addFirst(new OptionalSslHandler(finalSslContext));
                                }
                                sh.pipeline()
                                        .addLast("codec", new HttpServerCodec())
                                        .addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024))
                                        .addLast(new JsonRpcHttpServerHandler(applicationContext));
                            }
                        });
                ChannelFuture future;
                future = sb.bind(port).sync();
                countDownLatch.countDown();

                if (future.isSuccess()) {
                    log.info("JsonRpc http server startup successfully.");
                } else {
                    log.info("JsonRpc http server startup failed.");
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
        start();
    }
}
