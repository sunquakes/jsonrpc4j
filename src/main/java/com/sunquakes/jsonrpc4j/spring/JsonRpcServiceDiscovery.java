package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.discovery.Driver;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/1 8:17 PM
 **/
@Slf4j
public class JsonRpcServiceDiscovery {

    private Driver driver;

    private static JsonRpcServiceDiscovery instance;

    private static final List<Supplier<Boolean>> services = new CopyOnWriteArrayList<>();

    public static final ScheduledExecutorService retryThread = Executors.newScheduledThreadPool(2);

    protected static final Map<String, Future<?>> retryMap = new ConcurrentHashMap<>();

    public static final int REGISTRY_RETRY_INTERVAL = 3000;

    private JsonRpcServiceDiscovery(String url, String driverName) {

        ServiceLoader<Driver> driverServiceLoader = ServiceLoader.load(Driver.class);
        Iterator<Driver> driverIterator = driverServiceLoader.iterator();

        while (driverIterator.hasNext()) {
            Driver d = driverIterator.next();
            if (d.getClass().getName().equals(driverName)) {
                driver = d.newClient(url);
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

    @Synchronized
    public static boolean addService(Supplier<Boolean> service) {
        return services.add(service);
    }

    @Synchronized
    public static List<Supplier<Boolean>> getServices() {
        return services;
    }

    public Driver getDriver() {
        return this.driver;
    }
}