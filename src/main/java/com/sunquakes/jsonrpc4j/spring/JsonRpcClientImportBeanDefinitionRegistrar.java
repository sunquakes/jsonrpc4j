package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.JsonRpcClientHandlerInterface;
import com.sunquakes.jsonrpc4j.client.JsonRpcClientInvocationHandler;
import com.sunquakes.jsonrpc4j.client.JsonRpcHttpClientHandler;
import com.sunquakes.jsonrpc4j.client.JsonRpcTcpClientHandler;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@Slf4j
public class JsonRpcClientImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        String appClassName = annotationMetadata.getClassName();
        Class<?> appClass = null;
        try {
            appClass = Class.forName(appClassName);
        } catch (ClassNotFoundException e) {
            log.error("The import class of JsonRpcClient is not exists.");
        }
        String packageName = appClass.getPackage().getName();

        ClassPathScanningCandidateComponentProvider classPathScanningCandidateComponentProvider = new ClassPathScanningCandidateComponentProvider(false);
        classPathScanningCandidateComponentProvider.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {

                Map<String, Object> annotationAttributes = metadataReader
                        .getAnnotationMetadata()
                        .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcClient");

                if (annotationAttributes == null)
                    return false;

                Class<?> interfaceType = null;
                try {
                    interfaceType = Class.forName(metadataReader.getClassMetadata().getClassName());

                    // Select handler according to different protocol
                    String protocol = annotationAttributes.get("protocol").toString();
                    String url = annotationAttributes.get("url").toString();
                    if (!StringUtils.hasLength(url)) {
                        log.error("The url of JsonRpcClient is required.");
                        return false;
                    }
                    JsonRpcClientHandlerInterface jsonRpcClientHandler = getJsonRpcClientHandler(protocol, url);

                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcClientFactoryBean.class);
                    builder.addConstructorArgValue(interfaceType);
                    builder.addPropertyValue("jsonRpcClientHandler", jsonRpcClientHandler);
                    builder.addPropertyValue("service", annotationAttributes.get("value"));
                    beanDefinitionRegistry.registerBeanDefinition(interfaceType.getName(), builder.getBeanDefinition());
                    return true;
                } catch (ClassNotFoundException e) {
                    log.error("The interface of JsonRpcClient is not exists.");
                    return false;
                }
            }
        });
        classPathScanningCandidateComponentProvider.findCandidateComponents(packageName);
    }

    private JsonRpcClientHandlerInterface getJsonRpcClientHandler(String protocol, String url) throws IllegalArgumentException {
        if (protocol.equals(RequestUtils.PROTOCOL_TCP)) {
            return new JsonRpcTcpClientHandler();
        } else {
            return new JsonRpcHttpClientHandler(url);
        }
    }
}