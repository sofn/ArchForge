package ${packageBase};

import ${packageBase}.dto.${entityName}CreateRequest;
import ${packageBase}.dto.${entityName}UpdateRequest;
import ${packageBase}.service.${entityName}Service;
<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

<#macro defaultValue col>
<#if col.isString || col.isText || col.isJson || col.isFile>"测试"</#if>
<#if col.isInteger>1L</#if>
<#if col.isDecimal>new BigDecimal("1.00")</#if>
<#if col.isBoolean>true</#if>
<#if col.isDate>LocalDate.now()</#if>
<#if col.isDateTime>LocalDateTime.now()</#if>
<#if col.isEnum><#if col.hasOptions && col.options?size gt 0>${col.options[0].value}<#else>"A"</#if></#if>
</#macro>

@SpringBootTest(classes = GeneratedTestApplication.class)
@Transactional
class ${entityName}IntegrationTest {

    @Autowired
    private ${entityName}Service service;

    @Test
    void shouldCreateAndFind() {
        ${entityName}CreateRequest request = new ${entityName}CreateRequest();
<#list columns as col>
        request.set${col.fieldName?cap_first}(<@defaultValue col/>);
</#list>
        Long id = service.create(request);
        assertThat(id).isNotNull();
        assertThat(service.detail(id)).isNotNull();
    }

    @Test
    void shouldUpdate() {
        ${entityName}CreateRequest create = new ${entityName}CreateRequest();
<#list columns as col>
        create.set${col.fieldName?cap_first}(<@defaultValue col/>);
</#list>
        Long id = service.create(create);

        ${entityName}UpdateRequest update = new ${entityName}UpdateRequest();
        update.setId(id);
<#list columns as col>
        update.set${col.fieldName?cap_first}(<@defaultValue col/>);
</#list>
        assertThat(service.update(update)).isTrue();
        assertThat(service.detail(id)).isNotNull();
    }

    @Test
    void shouldDelete() {
        ${entityName}CreateRequest request = new ${entityName}CreateRequest();
<#list columns as col>
        request.set${col.fieldName?cap_first}(<@defaultValue col/>);
</#list>
        Long id = service.create(request);
        assertThat(service.delete(id)).isTrue();
        assertThat(service.getById(id).isPresent()).isFalse();
    }
}
