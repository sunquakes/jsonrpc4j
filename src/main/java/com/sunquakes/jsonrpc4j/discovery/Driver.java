package com.sunquakes.jsonrpc4j.discovery;

public interface Driver {

    Driver newClient(String url);

    void register(String name, String protocol, String hostname, int port);

    String get(String name);
}
