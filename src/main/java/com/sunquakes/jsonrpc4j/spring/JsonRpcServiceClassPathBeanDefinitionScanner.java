package com.sunquakes.jsonrpc4j.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/6/2 1:19 PM
 **/
public class JsonRpcServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public JsonRpcServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                Map<String, Object> annotationAttributes;
                String[] t = metadataReader.getClassMetadata().getInterfaceNames();
                for (int i = 0; i < t.length; i++) {
                    annotationAttributes = metadataReaderFactory.getMetadataReader(t[i])
                            .getAnnotationMetadata()
                            .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                    if (annotationAttributes != null) {
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
            BeanDefinitionRegistry registry = getRegistry();
            Assert.notEmpty(basePackages, "At least one base package must be specified");
            Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
            Map<String, Object> annotationAttributes;
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
                for (BeanDefinition candidate : candidates) {
                    String[] t = getMetadataReaderFactory().getMetadataReader(candidate.getBeanClassName()).getAnnotationMetadata().getInterfaceNames();
                    for (int i = 0; i < t.length; i++) {
                        annotationAttributes = getMetadataReaderFactory().getMetadataReader(t[i])
                                .getAnnotationMetadata()
                                .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                        if (annotationAttributes != null) {
                            String customBeanName = annotationAttributes.get("value").toString();
                            if (StringUtils.hasLength(customBeanName) && checkCandidate(customBeanName, candidate)) {
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, customBeanName);
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
