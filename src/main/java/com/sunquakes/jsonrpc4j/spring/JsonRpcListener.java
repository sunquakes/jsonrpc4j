package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/13 7:43 PM
 **/
public class JsonRpcListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(null == event.getApplicationContext().getParent()) {
            JsonRpcServiceDiscovery.getServices().forEach(item -> item.get());
        }
    }
}
