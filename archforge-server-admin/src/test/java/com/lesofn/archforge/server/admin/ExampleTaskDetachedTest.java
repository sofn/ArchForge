package com.lesofn.archforge.server.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootTest(classes = Application.class)
class ExampleTaskDetachedTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    void productionContextHasNoTaskPersistenceUnit() {
        assertFalse(applicationContext.containsBean("taskEntityManagerFactory"));
        assertFalse(applicationContext.containsBean("taskTransactionManager"));
        assertFalse(applicationContext.containsBean("taskDbConfig"));
    }

    @Test
    void productionContextHasNoTaskDatasource() {
        assertFalse(environment.containsProperty("spring.datasource.dynamic.datasource.task_master.url"));
        assertFalse(environment.containsProperty("spring.datasource.dynamic.datasource.task_slave.url"));
    }
}
