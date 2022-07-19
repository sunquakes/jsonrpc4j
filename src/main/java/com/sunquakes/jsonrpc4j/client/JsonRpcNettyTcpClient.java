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
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.0
 * @since : 2022/6/28 9:05 PM
 **/
@Slf4j
public class JsonRpcNettyTcpClient implements JsonRpcClientHandlerInterface {

    private Integer DEFAULT_PORT = 80;

    private String url;

    private String IP;

    private Integer PORT;

    private TcpClientOption tcpClientOption;

    private JsonRpcNettyTcpClientHandler jsonRpcNettyTcpClientHandler;

    private static ConcurrentHashMap bootstrapMap = new ConcurrentHashMap();

    public JsonRpcNettyTcpClient(String url, TcpClientOption tcpClientOption) {
        jsonRpcNettyTcpClientHandler = new JsonRpcNettyTcpClientHandler(tcpClientOption);
        this.tcpClientOption = tcpClientOption;
        this.url = url;
        Object[] ipPort = getIpPort(url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        Channel channel = getChannel();
        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);

        SynchronousQueue<Object> queue = jsonRpcNettyTcpClientHandler.send(request, channel);
        String body = (String) queue.take();
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
    private Channel getChannel() throws Exception {
        Bootstrap bootstrap = (Bootstrap) bootstrapMap.get(url);
        if (bootstrap == null) {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(IP, PORT))
                    .option(ChannelOption.SO_RCVBUF, tcpClientOption.getPackageMaxLength())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ByteArrayDecoder())
                                    .addLast(new ByteArrayEncoder())
                                    .addLast(jsonRpcNettyTcpClientHandler);
                        }
                    });
        }
        ObjectPool<Channel> pool = JsonRpcNettyChannelPoolFactory.getPool(url, new JsonRpcNettyChannelFactory(bootstrap));
        Channel channel = pool.borrowObject();
        return channel;
    }

    private Object[] getIpPort(String url) {
        String[] ipPort = url.split(":");
        String ip = ipPort[0];
        Integer port;
        if (ipPort.length < 2) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.valueOf(ipPort[1]);
        }
        return new Object[]{ip, port};
    }
}
