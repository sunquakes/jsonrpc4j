package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({JsonRpcImportBeanDefinitionRegistrar.class})
public @interface JsonRpcScan {

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     * @return base packages
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     * @return base packages
     */
    @AliasFor("value")
    String[] basePackages() default {};
}
