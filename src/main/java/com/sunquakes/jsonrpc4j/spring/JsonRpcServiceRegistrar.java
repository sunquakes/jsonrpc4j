package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.governance.Driver;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/1 8:17 PM
 **/
@Slf4j
public class JsonRpcServiceRegistrar {

    private String url;

    private String driverName;

    public JsonRpcServiceRegistrar(String url, String driverName) {
        this.url = url;
        this.driverName = driverName;
    }

    public void registerService(String name, int port) {
        ServiceLoader<Driver> driverServiceLoader = ServiceLoader.load(Driver.class);
        Iterator<Driver> driverIterator = driverServiceLoader.iterator();

        while (driverIterator.hasNext()) {
            Driver driver = driverIterator.next();
            System.out.println(driver.getClass().getName());
            System.out.println(driverName);
            if (driver.getClass().getName().equals(driverName)) {
                driver.newClient(url).register(name, port);
            }
        }
    }
}