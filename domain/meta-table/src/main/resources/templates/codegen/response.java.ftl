package ${packageBase}.dto;

<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
import lombok.Data;

@Data
public class ${entityName}Response {

    private Long id;

<#list listVisibleColumns as col>
    private ${col.javaType} ${col.fieldName};
</#list>

    private Long createTime;
}
