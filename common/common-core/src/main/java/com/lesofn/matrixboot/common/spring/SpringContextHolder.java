package com.lesofn.matrixboot.common.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-14 20:06
 */
@Slf4j
@Component
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    @Override
    public void destroy() {
        log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
        SpringContextHolder.applicationContext = null;
    }

    public static ApplicationContext getApplicationContext() {
        if (SpringContextHolder.applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    /**
     * 获取property信息
     *
     * @param property 属性key
     * @return value
     */
    public static String getProperty(String property) {
        return getProperty(property, String.class, null);
    }

    /**
     * 获取property信息
     *
     * @param property     属性key
     * @param clazz        类型
     * @param defaultValue 默认值
     * @param <T>          类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(String property, Class<T> clazz, T defaultValue) {
        return Optional.ofNullable(applicationContext)
                .map(EnvironmentCapable::getEnvironment)
                .map(it -> it.getProperty(property, clazz, defaultValue))
                .orElse(Optional.ofNullable(applicationContext)
                        .map(it -> it.getBeansOfType(PropertySourcesPlaceholderConfigurer.class).values())
                        .flatMap(it -> it.stream()
                                .map(PropertySourcesPlaceholderConfigurer::getAppliedPropertySources)
                                .map(aps -> aps.get("localProperties"))
                                .filter(Objects::nonNull)
                                .filter(p -> p.getProperty(property) != null)
                                .findFirst()
                        )
                        .map(it -> (T) it.getProperty(property))
                        .orElse(defaultValue)
                );
    }

    public static boolean isInjectedApplicationContext() {
        return SpringContextHolder.applicationContext != null;
    }
}
