package com.sunquakes.jsonrpc4j.client;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.Socket;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/28 10:44 AM
 **/
public class JsonRpcTcpFactory extends BasePooledObjectFactory<Socket> {

    private Integer DEFAULT_PORT = 80;

    private String IP;

    private Integer PORT;

    public JsonRpcTcpFactory(String url) {
        Object[] ipPort = getIpPort(url);
        IP = (String) ipPort[0];
        PORT = (Integer) ipPort[1];
    }

    @Override
    public Socket create() throws Exception {
        return new Socket(IP, PORT);
    }

    @Override
    public PooledObject<Socket> wrap(Socket obj) {
        PooledObject<Socket> pooledObject = new DefaultPooledObject(obj);
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
