package com.lesofn.appforge.infrastructure.frame.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

/**
 * Removes the springdoc QueryDSL bean that is incompatible with Spring Data 4.x. In Spring Data
 * 4.x, TypeInformation was relocated from org.springframework.data.util to
 * org.springframework.data.core, breaking springdoc 2.x's QuerydslPredicateOperationCustomizer.
 *
 * <p>TODO: Remove this workaround when upgrading to a springdoc version compatible with Spring Boot
 * 4.
 *
 * @author sofn
 */
@Component
public class SpringDocQuerydslExcluder implements BeanFactoryPostProcessor {

    private static final String QUERYDSL_BEAN_NAME = "queryDslQuerydslPredicateOperationCustomizer";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (beanFactory instanceof BeanDefinitionRegistry registry) {
            if (registry.containsBeanDefinition(QUERYDSL_BEAN_NAME)) {
                registry.removeBeanDefinition(QUERYDSL_BEAN_NAME);
            }
        }
    }
}
