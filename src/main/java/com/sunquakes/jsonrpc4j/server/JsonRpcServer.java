package com.sunquakes.jsonrpc4j.server;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 1.0.0
 * @since 1.0.0
 **/
public abstract class JsonRpcServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
