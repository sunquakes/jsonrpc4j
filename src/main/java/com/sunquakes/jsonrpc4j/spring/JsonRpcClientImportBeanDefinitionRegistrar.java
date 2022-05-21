package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.JsonRpcClientInvocationHandler;
import com.sunquakes.jsonrpc4j.client.JsonRpcHttpClientHandler;
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
public class JsonRpcClientImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Boolean enable = importingClassMetadata
                .getAnnotations()
                .stream()
                .filter(x -> x.getType().equals(JsonRpcScan.class))
                .count() > 0;

        // if (!enable)
        //     return;

        try {
            String appClassName = importingClassMetadata.getClassName();
            Class<?> appClass = Class.forName(appClassName);
            String packageName = appClass.getPackage().getName();

            List<Class<?>> interfaceTypes = new LinkedList<Class<?>>();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new TypeFilter() {
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
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    interfaceTypes.add(interfaceType);
                    return true;
                }
            });
            scanner.findCandidateComponents(packageName);

            for (Class<?> type : interfaceTypes) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcClientFactoryBean.class);
                builder.addConstructorArgValue(type);
                builder.addConstructorArgValue(new JsonRpcHttpClientHandler());
                registry.registerBeanDefinition("client", builder.getBeanDefinition());
            }

        } catch (Exception e) {
            e.printStackTrace();
            // error code here
        }
    }
}