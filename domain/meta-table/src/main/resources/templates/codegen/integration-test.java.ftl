package ${packageBase};

import ${packageBase}.dto.${entityName}CreateRequest;
import ${packageBase}.dto.${entityName}UpdateRequest;
import ${packageBase}.service.${entityName}Service;
<#list imports as imp>
import ${imp};
</#list>
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GeneratedTestApplication.class)
@Testcontainers
@Transactional
class ${entityName}IntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.dynamic.primary", () -> "user_master");
        registry.add("spring.datasource.dynamic.datasource.user_master.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.dynamic.datasource.user_master.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.dynamic.datasource.user_master.username", postgres::getUsername);
        registry.add("spring.datasource.dynamic.datasource.user_master.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ${entityName}Service service;

    @Test
    void shouldCreateAndFind() {
        ${entityName}CreateRequest request = new ${entityName}CreateRequest();
<#list columns as col>
        request.set${col.fieldName?cap_first}(${col.javaDefaultValue});
</#list>
        Long id = service.create(request);
        assertThat(id).isNotNull();
        assertThat(service.detail(id)).isNotNull();
    }

    @Test
    void shouldUpdate() {
        ${entityName}CreateRequest create = new ${entityName}CreateRequest();
<#list columns as col>
        create.set${col.fieldName?cap_first}(${col.javaDefaultValue});
</#list>
        Long id = service.create(create);

        ${entityName}UpdateRequest update = new ${entityName}UpdateRequest();
        update.setId(id);
<#list columns as col>
        update.set${col.fieldName?cap_first}(${col.javaDefaultValue});
</#list>
        assertThat(service.update(update)).isTrue();
        assertThat(service.detail(id)).isNotNull();
    }

    @Test
    void shouldDelete() {
        ${entityName}CreateRequest request = new ${entityName}CreateRequest();
<#list columns as col>
        request.set${col.fieldName?cap_first}(${col.javaDefaultValue});
</#list>
        Long id = service.create(request);
        assertThat(service.delete(id)).isTrue();
        assertThat(service.getById(id).isPresent()).isFalse();
    }
}
