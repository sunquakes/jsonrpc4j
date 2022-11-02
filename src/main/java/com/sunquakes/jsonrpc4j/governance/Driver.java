package com.sunquakes.jsonrpc4j.governance;

public interface Driver {

    Driver newClient(String url);

    void register(String name, int port);
}
