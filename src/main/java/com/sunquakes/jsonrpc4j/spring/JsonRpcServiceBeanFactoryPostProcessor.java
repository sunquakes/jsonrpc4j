package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.ProtocolEnum;
import com.sunquakes.jsonrpc4j.server.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.server.JsonRpcNettyHttpServer;
import com.sunquakes.jsonrpc4j.server.JsonRpcNettyTcpServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
public class JsonRpcServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        if (defaultListableBeanFactory == null) {
            return;
        }
        String protocol = environment.getProperty("jsonrpc.server.protocol");
        String port = environment.getProperty("jsonrpc.server.port");
        String serverBeanName = String.format("%s_%s_%s", "JsonRpcServer", protocol, port);
        if (protocol != null && port != null && !defaultListableBeanFactory.containsBeanDefinition(serverBeanName)) {
            BeanDefinitionBuilder serverBuilder;
            if (protocol.equals(ProtocolEnum.Tcp.getName())) {
                serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcNettyTcpServer.class);
            } else if (protocol.equals(ProtocolEnum.Http.getName()) || protocol.equals(ProtocolEnum.Https.getName())) {
                serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcNettyHttpServer.class);
            } else {
                throw new IllegalArgumentException("Invalid protocol.");
            }
            defaultListableBeanFactory.registerBeanDefinition(serverBeanName, serverBuilder.getBeanDefinition());
        }
    }
}
