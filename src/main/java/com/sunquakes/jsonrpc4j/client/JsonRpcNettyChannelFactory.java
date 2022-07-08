package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 10:44 AM
 **/
public class JsonRpcNettyChannelFactory extends BasePooledObjectFactory<Channel> {

    private Integer DEFAULT_PORT = 80;

    private Bootstrap bootstrap;

    public JsonRpcNettyChannelFactory(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public Channel create() throws Exception {
        return bootstrap.connect().sync().channel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        PooledObject<Channel> pooledObject = new DefaultPooledObject(obj);
        return pooledObject;
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
