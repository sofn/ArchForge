package com.lesofn.archforge.server.admin.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 保证 Flyway 在 JPA 的 EntityManagerFactory 构建之前执行迁移。
 *
 * <p>
 * UserDbConfig 与 TaskDbConfig 定义在 domain 模块，无法直接感知 server-admin 中的 Flyway
 * bean。此 post-processor 在 Flyway bean 存在时，为 domain 的 EMF 配置类添加 dependsOn，
 * 确保 schema 先由 Flyway 创建，再被 Hibernate validate/update。
 */
@Component
public class FlywayDependencyBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (!beanFactory.containsBeanDefinition("flyway")) {
            return;
        }

        String[] targetNames = {
                "userDbConfig", "taskDbConfig", "blogDbConfig", "userDomainDbConfig"
        };
        for (String name : targetNames) {
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
