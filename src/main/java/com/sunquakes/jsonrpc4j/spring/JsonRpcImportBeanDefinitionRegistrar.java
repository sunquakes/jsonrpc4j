package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.ProtocolEnum;
import com.sunquakes.jsonrpc4j.server.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.server.JsonRpcTcpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Slf4j
public class JsonRpcImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String, Object> data = annotationMetadata.getAnnotationAttributes("com.sunquakes.jsonrpc4j.spring.JsonRpcScan");
        String[] basePackages = (String[]) data.get("basePackages");

        ClassPathScanningCandidateComponentProvider classPathScanningCandidateComponentProvider = new JsonRpcClientClassPathScanningCandidateComponentProvider(false, environment, beanDefinitionRegistry);
        ClassPathBeanDefinitionScanner scanner = new JsonRpcServiceClassPathBeanDefinitionScanner(beanDefinitionRegistry);
        for (String basePackage : basePackages) {
            classPathScanningCandidateComponentProvider.findCandidateComponents(basePackage);
            scanner.scan(basePackage);
        }
    }
}