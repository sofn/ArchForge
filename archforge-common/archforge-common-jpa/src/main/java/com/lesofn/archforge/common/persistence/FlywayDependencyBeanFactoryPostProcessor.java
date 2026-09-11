package com.lesofn.archforge.common.persistence;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 保证 Flyway 在各 domain 模块的 JPA EntityManagerFactory 构建之前执行迁移。
 *
 * <p>
 * user / blog / user-domain / meta-table 的 EMF 配置类分布在 domain 模块中，看不见 Flyway bean。本
 * post-processor 在 Flyway 存在时给这些配置类 bean 追加 dependsOn——@Bean 方法产出的 EMF 依赖其配置类实例，
 * 从而传递性地保证"先 migrate、再 validate"。server-admin 与 server-web 共用本类（common-jpa 包被两个
 * 应用的组件扫描覆盖）。
 */
@Component
public class FlywayDependencyBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private static final String[] EMF_CONFIG_BEANS = {
            "userDbConfig", "blogDbConfig", "userDomainDbConfig", "metaTableDbConfig"
    };

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBeanDefinition("flyway")) {
            return;
        }
        for (String name : EMF_CONFIG_BEANS) {
            if (!beanFactory.containsBeanDefinition(name)) {
                continue;
            }
            AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanFactory.getBeanDefinition(name);
            String[] existing = beanDefinition.getDependsOn();
            if (existing == null) {
                beanDefinition.setDependsOn("flyway");
            } else {
                String[] merged = new String[existing.length + 1];
                System.arraycopy(existing, 0, merged, 0, existing.length);
                merged[existing.length] = "flyway";
                beanDefinition.setDependsOn(merged);
            }
        }
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
