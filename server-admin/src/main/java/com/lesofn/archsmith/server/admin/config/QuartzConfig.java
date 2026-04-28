package com.lesofn.archsmith.server.admin.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Wires Spring's {@link AutowireCapableBeanFactory} into Quartz's {@link SpringBeanJobFactory} so
 * reflective Jobs can resolve Spring beans (e.g. ApplicationContext, repositories) via
 * {@code @Autowired}.
 *
 * @author sofn
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext ctx) {
        AutowiringSpringBeanJobFactory f = new AutowiringSpringBeanJobFactory();
        f.setApplicationContext(ctx);
        return f;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer customizeScheduler(SpringBeanJobFactory jobFactory) {
        return bean -> bean.setJobFactory(jobFactory);
    }

    /** Subclass of SpringBeanJobFactory that autowires Job instances after creation. */
    static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
            implements ApplicationContextAware {

        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(ApplicationContext ctx) {
            this.beanFactory = ctx.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}
