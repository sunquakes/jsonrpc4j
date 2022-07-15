package com.sunquakes.jsonrpc4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 10:44 AM
 **/
public class JsonRpcNettyChannelFactory extends BasePooledObjectFactory<Channel> {

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
}
