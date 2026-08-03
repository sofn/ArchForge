package ${packageBase}.dto;

<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
import lombok.Data;

@Data
public class ${entityName}ListRequest {

    private Integer currentPage = 1;

    private Integer pageSize = 10;

    private String keyword;

<#list searchableColumns as col>
    private ${col.javaType} ${col.fieldName};
</#list>
}
