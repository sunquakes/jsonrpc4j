package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 **/
public class JsonRpcServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        JsonRpcServiceBeanFactoryPostProcessor jsonRpcServiceBeanFactoryPostProcessor = new JsonRpcServiceBeanFactoryPostProcessor();
        jsonRpcServiceBeanFactoryPostProcessor.setEnvironment(environment);
        applicationContext.addBeanFactoryPostProcessor(jsonRpcServiceBeanFactoryPostProcessor);
    }
}
