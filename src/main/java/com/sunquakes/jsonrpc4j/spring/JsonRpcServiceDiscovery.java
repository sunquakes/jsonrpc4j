package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.discovery.Driver;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/1 8:17 PM
 **/
@Slf4j
public class JsonRpcServiceDiscovery {

    private Driver driver;

    private static JsonRpcServiceDiscovery instance;

    private JsonRpcServiceDiscovery(String url, String driverName) {

        ServiceLoader<Driver> driverServiceLoader = ServiceLoader.load(Driver.class);
        Iterator<Driver> driverIterator = driverServiceLoader.iterator();

        while (driverIterator.hasNext()) {
            Driver driver = driverIterator.next();
            if (driver.getClass().getName().equals(driverName)) {
                this.driver = driver.newClient(url);
                break;
            }
        }
    }

    @Synchronized
    public static JsonRpcServiceDiscovery newInstance(String url, String driverName) {
        if (instance == null) {
            instance = new JsonRpcServiceDiscovery(url, driverName);
        }
        return instance;
    }

    public Driver getDriver() {
        return this.driver;
    }
}