package com.sunquakes.jsonrpc4j.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/6/2 1:19 PM
 **/
public class JsonRpcServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public JsonRpcServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                // if (annotationAttributes == null)
                //     return false;
                System.out.println("9999999999999");
                Map<String, Object> annotationAttributes;
                System.out.println(metadataReader.getClassMetadata().getClassName());
                String[] t = metadataReader.getClassMetadata().getInterfaceNames();
                for (int i = 0; i < t.length; i++) {
                    annotationAttributes = metadataReaderFactory.getMetadataReader(t[i])
                            .getAnnotationMetadata()
                            .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                    if (annotationAttributes != null) {
                        System.out.println("0000000000000000000");
                        System.out.println(t[i]);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        try {

            JsonRpcServiceBeanNameGenerator beanNameGenerator = new JsonRpcServiceBeanNameGenerator();
            BeanDefinitionRegistry registry = getRegistry();
            Assert.notEmpty(basePackages, "At least one base package must be specified");
            Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
            Map<String, Object> annotationAttributes;
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
                for (BeanDefinition candidate : candidates) {
                    String[] t = getMetadataReaderFactory().getMetadataReader(candidate.getBeanClassName()).getAnnotationMetadata().getInterfaceNames();
                    for (int i = 0; i < t.length; i++) {
                        System.out.println("------------------");
                        annotationAttributes = getMetadataReaderFactory().getMetadataReader(t[i])
                                .getAnnotationMetadata()
                                .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                        if (annotationAttributes != null) {
                            String beanName = beanNameGenerator.generateBeanName(candidate, registry);
                            if (checkCandidate(beanName, candidate)) {
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                                beanDefinitions.add(definitionHolder);
                                registerBeanDefinition(definitionHolder, registry);
                            }
                        }
                    }
                }
            }
            return beanDefinitions;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && (metadata.isConcrete() || metadata.isInterface());
    }
}
