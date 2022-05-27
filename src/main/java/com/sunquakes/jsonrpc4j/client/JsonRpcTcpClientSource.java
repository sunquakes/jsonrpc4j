package com.sunquakes.jsonrpc4j.client;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.client
 * @Author: Robert
 * @CreateTime: 2022/5/27 12:36 PM
 **/
@UtilityClass
public class JsonRpcTcpClientSource {

    private Integer DEFAULT_PORT = 80;

    Map<String, LinkedList<Socket>> poolMap = new HashMap<>();

    public void initPool(String url, int size) {
        for (int i = 0; i < size; i++) {
            addSocket(url);
        }
    }

    public LinkedList<Socket> getPool(String url) {
        Object[] ipPort = getIpPort(url);
        String ip = (String) ipPort[0];
        Integer port = (Integer) ipPort[1];
        String key = String.format("%s_%d", ip, port);

        LinkedList<Socket> pool = poolMap.getOrDefault(key, new LinkedList<>());
        return pool;
    }

    public String getKey(String url) {
        Object[] ipPort = getIpPort(url);
        String ip = (String) ipPort[0];
        Integer port = (Integer) ipPort[1];
        String key = String.format("%s_%d", ip, port);
        return key;
    }

    public void addSocket(String url) {
        try {
            LinkedList<Socket> pool = poolMap.getOrDefault(getKey(url), new LinkedList<>());

            Object[] ipPort = getIpPort(url);
            String ip = (String) ipPort[0];
            Integer port = (Integer) ipPort[1];
            Socket socket = new Socket(ip, port);
            pool.addLast(socket);
            poolMap.put(getKey(url), pool);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket(String url) {
        LinkedList<Socket> pool = poolMap.getOrDefault(getKey(url), new LinkedList<>());
        Socket socket = pool.removeFirst();
        return socket;
    }

    public void backToPool(String url, Socket socket) {
        if (socket != null && !socket.isClosed()) {
            LinkedList<Socket> pool = getPool(url);
            pool.addLast(socket);
        }
    }

    public Object[] getIpPort(String url) {
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
