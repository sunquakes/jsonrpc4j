package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.server.JsonRpcHttpServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.*;

import static java.lang.String.format;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;

public class JsonRpcServerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static Map<String, String> findServiceBeanDefinitions(ConfigurableListableBeanFactory beanFactory) {
        final Map<String, String> serviceBeanNames = new HashMap<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            serviceBeanNames.put(beanName, beanName);
        }
        return serviceBeanNames;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        Map<String, String> servicePathToBeanName = findServiceBeanDefinitions(defaultListableBeanFactory);
        for (Map.Entry<String, String> entry : servicePathToBeanName.entrySet()) {
            registerServerProxy(defaultListableBeanFactory, entry.getValue());
        }
    }

    private void registerServerProxy(DefaultListableBeanFactory defaultListableBeanFactory, String beanName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcHttpServer.class);
        defaultListableBeanFactory.registerBeanDefinition("JsonRpcServer", builder.getBeanDefinition());
    }

    private BeanDefinition findBeanDefinition(ConfigurableListableBeanFactory beanFactory, String serviceBeanName) {
        if (beanFactory.containsLocalBean(serviceBeanName)) {
            return beanFactory.getBeanDefinition(serviceBeanName);
        }
        BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
        if (parentBeanFactory != null && ConfigurableListableBeanFactory.class.isInstance(parentBeanFactory)) {
            return findBeanDefinition((ConfigurableListableBeanFactory) parentBeanFactory, serviceBeanName);
        }
        throw new NoSuchBeanDefinitionException(serviceBeanName);
    }

    private Class<?>[] getBeanInterfaces(BeanDefinition serviceBeanDefinition, ClassLoader beanClassLoader) {
        String beanClassName = serviceBeanDefinition.getBeanClassName();
        try {
            Class<?> beanClass = forName(beanClassName, beanClassLoader);
            return getAllInterfacesForClass(beanClass, beanClassLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException(format("Cannot find bean class '%s'.", beanClassName), e);
        }
    }
}
