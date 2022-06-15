package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcService;
import com.sunquakes.jsonrpc4j.ProtocolEnum;
import com.sunquakes.jsonrpc4j.server.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.server.JsonRpcTcpServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;

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
                serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcTcpServer.class);
            } else if (protocol.equals(ProtocolEnum.Http.getName())) {
                serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcHttpServer.class);
            } else {
                throw new IllegalArgumentException("Invalid protocol.");
            }
            defaultListableBeanFactory.registerBeanDefinition(serverBeanName, serverBuilder.getBeanDefinition());
        }
    }
}
