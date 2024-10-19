package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.dto.ErrorDto;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcTcpClient extends JsonRpcClient implements JsonRpcClientInterface {

    private JsonRpcTcpClientHandler jsonRpcTcpClientHandler;

    public JsonRpcTcpClient(Config config) {
        super(config);
    }

    @Override
    public void initLoadBalancer() {
        JsonRpcChannelPoolHandler poolHandler = new JsonRpcChannelPoolHandler(new Handler());
        String packageEof = (String) config.get("packageEof").value();
        int packageMaxLength = (int) config.get("packageMaxLength").value();
        TcpClientOption tcpClientOption = new TcpClientOption(packageEof, packageMaxLength);
        jsonRpcTcpClientHandler = new JsonRpcTcpClientHandler(tcpClientOption);
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, tcpClientOption.getPackageMaxLength())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ByteArrayDecoder())
                                .addLast(new ByteArrayEncoder())
                                .addLast(jsonRpcTcpClientHandler);
                    }
                });

        int defaultPort = 80;
        if (discovery != null) {
            loadBalancer = new JsonRpcLoadBalancer(() -> discovery.value().get(name), defaultPort, bootstrap, poolHandler);
        } else {
            loadBalancer = new JsonRpcLoadBalancer(() -> url.value(), defaultPort, bootstrap, poolHandler);
        }
    }

    @Override
    public Object handle(String method, Object[] args) throws JsonRpcException {
        RequestDto requestDto = new RequestDto(RequestUtils.getId(), RequestUtils.JSONRPC, method, args);
        ResponseDto responseDto;
        String body;
        FixedChannelPool pool = loadBalancer.getPool();
        try {
            Channel channel = pool.acquire().get();
            body = jsonRpcTcpClientHandler.send(requestDto, channel);
            pool.release(channel);
            responseDto = JSONUtils.toJavaObject(ResponseDto.class, body);
        } catch (InterruptedException e) {
            loadBalancer.removePool(pool);
            Thread.currentThread().interrupt();
            throw new JsonRpcClientException(e.getMessage());
        } catch (ExecutionException e) {
            throw new JsonRpcClientException(e.getMessage());
        }
        if (responseDto.getResult() == null) {
            Object error = JSONUtils.get(JSONUtils.parseJSONObject(body), "error");
            if (error != null) {
                ErrorDto errorDto = JSONUtils.parseJavaObject(JSONUtils.toString(error), ErrorDto.class);
                throw ErrorEnum.getException(errorDto.getCode(), errorDto.getMessage());
            }
        }
        return responseDto.getResult();
    }

    class Handler implements JsonRpcChannelHandler {
        @Override
        public void channelUpdated(Channel ch) {
            ch.pipeline()
                    .addLast(new ByteArrayDecoder())
                    .addLast(new ByteArrayEncoder())
                    .addLast(jsonRpcTcpClientHandler);
        }
    }
}