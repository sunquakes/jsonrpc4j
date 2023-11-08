package com.sunquakes.jsonrpc4j.server;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
public abstract class JsonRpcServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
