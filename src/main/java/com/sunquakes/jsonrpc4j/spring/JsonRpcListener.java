package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.function.Supplier;

/**
 * @author Shing Rui <sunquakes@outlook.com>
 * @version 2.1.0
 * @since 1.0.0
 **/
public class JsonRpcListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (null == event.getApplicationContext().getParent()) {
            JsonRpcServiceDiscovery.getServices().forEach(Supplier::get);
        }
    }
}
