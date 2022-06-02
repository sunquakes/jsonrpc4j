package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
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
                String [] t = metadataReader.getClassMetadata().getInterfaceNames();
                for (int i = 0; i < t.length; i++) {
                    System.out.println(t[i]);
                    annotationAttributes = metadataReaderFactory.getMetadataReader(t[i])
                            .getAnnotationMetadata()
                            .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                    if (annotationAttributes != null)
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        ScannedGenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();
            String beanName = holder.getBeanName();
            System.out.println("88888888888888888");
            System.out.println(beanName);
            if (definition.getMetadata().isInterface()) {
            }
        }
        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && (metadata.isConcrete() || metadata.isInterface());
    }
}
